package com.example.lingoscroll.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lingoscroll.data.LearningContent
import com.example.lingoscroll.data.ItemType
import com.example.lingoscroll.data.local.AppDatabase
import com.example.lingoscroll.data.local.WordCard
import com.example.lingoscroll.data.Level
import com.example.lingoscroll.data.PreferencesManager
import com.example.lingoscroll.data.repository.CardRepository
import com.example.lingoscroll.data.tts.NativeTtsManager
import com.example.lingoscroll.data.tts.TtsManager
import com.example.lingoscroll.sync.SyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

sealed interface MainScreenUiState {
    object OnboardingWelcome : MainScreenUiState
    object OnboardingStyleSelection : MainScreenUiState
    
    data class OnboardingQuiz(
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val currentQuestion: WordCard,
        val selectedOption: String?,
        val correctCount: Int
    ) : MainScreenUiState

    data class OnboardingLevelReveal(
        val level: Level,
        val correctCount: Int
    ) : MainScreenUiState

    data class Practice(
        val currentItem: WordCard,
        val isMeaningRevealed: Boolean = false,
        val selectedOption: String? = null,
        val isAnswerEvaluated: Boolean = false,
        val isAnswerCorrect: Boolean = false,
        val streak: Int,
        val secondsSaved: Long,
        val currentLevel: Level,
        val secondsSpentToday: Long = 0L,
        // Aşama bazlı ilerleme alanları
        val currentStage: Int = 1,
        val stageProgress: Int = 0,
        val showStageComplete: Boolean = false,
        // Bağımsız Kelime Kartı Alanı
        val learningCards: List<WordCard> = emptyList(),
        val currentLearningCardIndex: Int = 0,
        val showLearningCards: Boolean = false,
        val isLearningCardRevealed: Boolean = false,
        val selectedStyle: String = "MIXED",
        val speechRate: Float = 1.0f
    ) : MainScreenUiState
}

class MainScreenViewModel(context: Context) : ViewModel() {
    private val prefs = PreferencesManager(context)
    private val tts: TtsManager = NativeTtsManager(context)
    private val db = AppDatabase.getDatabase(context, viewModelScope)
    private val repository = CardRepository(db.cardDao())

    private val _uiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.OnboardingWelcome)
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val pendingCardUpdates = mutableMapOf<Int, Boolean>() // cardId -> isCorrect
    private val sessionQueue = mutableListOf<WordCard>() // 15 soruluk aktif aşama kuyruğu
    private var lastSeenItemId: Int? = null
    private var selectedLevel: Level? = null
    private var secondsTimerActive = 0L
    private var activeDiagnosticQuestions = listOf<WordCard>()

    private fun isEnglishString(text: String): Boolean {
        val turkishChars = charArrayOf('ı', 'ş', 'ğ', 'ç', 'ö', 'ü', 'ı', 'Ş', 'Ğ', 'Ç', 'Ö', 'Ü', 'İ')
        return !text.any { it in turkishChars }
    }

    private fun getEnglishPart(text: String): String {
        val turkishChars = charArrayOf('ı', 'ş', 'ğ', 'ç', 'ö', 'ü', 'ı', 'Ş', 'Ğ', 'Ç', 'Ö', 'Ü', 'İ')
        val firstTurkishIndex = text.indexOfFirst { it in turkishChars }
        
        if (firstTurkishIndex != -1) {
            val sub = text.substring(0, firstTurkishIndex)
            val lastSpace = sub.lastIndexOf(" ")
            val cleaned = if (lastSpace != -1) sub.substring(0, lastSpace) else sub
            val trimmed = cleaned.trim(' ', '"', '\'', ':', ',', '.', '?')
            if (trimmed.isNotEmpty() && isEnglishString(trimmed) && trimmed.length > 3) {
                return trimmed
            }
        }
        
        val regex = "['\"]([^'\"]*)['\"]".toRegex()
        val matches = regex.findAll(text).map { it.groupValues[1] }.toList()
        val longestEnglishMatch = matches.filter { isEnglishString(it) }.maxByOrNull { it.length }
        if (longestEnglishMatch != null && longestEnglishMatch.trim().isNotEmpty()) {
            return longestEnglishMatch.trim()
        }
        
        return text
    }

    // Sadece İngilizce kısımları okumak için yardımcı fonksiyon (Cevap sızdırmaz)
    private fun getSpeakText(card: WordCard, isAnswerEvaluated: Boolean): String {
        return when (card.type) {
            "QUIZ_COMPLETION" -> {
                val phrase = card.expression
                val end = phrase.lastIndexOf("'")
                val rawSentence = if (end != -1) {
                     val start = phrase.lastIndexOf("'", end - 1)
                     if (start != -1) {
                         phrase.substring(start + 1, end)
                     } else {
                         phrase
                     }
                } else {
                     phrase
                }
                if (isAnswerEvaluated) {
                    rawSentence.replace("_____", card.correctAnswer)
                } else {
                    rawSentence.replace("_____", "blank")
                }
            }
            "QUIZ_MULTIPLE_CHOICE" -> {
                if (isAnswerEvaluated) {
                    if (isEnglishString(card.correctAnswer)) {
                        card.correctAnswer
                    } else {
                        getEnglishPart(card.expression)
                    }
                } else {
                    if (isEnglishString(card.expression)) {
                        card.expression
                    } else {
                        getEnglishPart(card.expression).ifEmpty { "Please choose the correct answer" }
                    }
                }
            }
            else -> card.expression
        }
    }

    init {
        // Her yeni yüklemede seviye tespit sınavının (SBS) baştan gelmesini sağlamak için build zamanı kontrolü
        val currentBuildTime = "20260701_1930" 
        val savedBuildTime = prefs.getSavedBuildTime()
        if (savedBuildTime != currentBuildTime) {
            prefs.clearUserProgress()
            prefs.clearRecentSeenIds()
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteAll()
                AppDatabase.loadOfflineQuestions(context, db.cardDao())
            }
            prefs.setSavedBuildTime(currentBuildTime)
        }

        tts.setSpeechRate(prefs.getTtsSpeechRate())
        triggerForegroundSync()
        scheduleBackgroundSync(context)
        scheduleNotifications(context)
        startActiveTimer()

        val savedLevel = prefs.getUserLevel()
        if (savedLevel != null) {
            selectedLevel = savedLevel
            prefs.updateStreak()
            startStageSession(savedLevel)
        } else {
            _uiState.value = MainScreenUiState.OnboardingWelcome
        }
    }

    // Aşama Oturum Başlatıcı (9 Yeni + 6 Tekrar asenkron birleştirme)
    fun startStageSession(level: Level) {
        viewModelScope.launch(Dispatchers.IO) {
            val category = prefs.getUserStyle()
            val exclude = prefs.getRecentSeenIds()
            val questions = repository.getStagePackage(level.name, category, exclude, System.currentTimeMillis())
            
            viewModelScope.launch(Dispatchers.Main) {
                sessionQueue.clear()
                sessionQueue.addAll(questions)
                loadNextPracticeItem(level)
            }
        }
    }

    // Yaşam Döngüsü tetiklendiğinde (onPause/onStop) RAM'deki verileri Room'a kaydetme
    fun saveProgress() {
        if (secondsTimerActive > 0L) {
            prefs.addSecondsSpentToday(secondsTimerActive)
            secondsTimerActive = 0L
        }

        val updates = pendingCardUpdates.toMap()
        pendingCardUpdates.clear()
        
        if (updates.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                updates.forEach { (cardId, isCorrect) ->
                    repository.updateCardProgress(cardId, isCorrect)
                }
            }
        }
    }

    // Tarz seçim ekranını başlatır
    fun startStyleSelection() {
        _uiState.value = MainScreenUiState.OnboardingStyleSelection
    }

    // Seçilen tarzı kaydeder ve seviye tespit sınavını başlatır (SBS)
    fun selectLearningStyle(style: String) {
        prefs.setUserStyle(style)
        
        // 10 soruluk dinamik SBS listesini oluştur (3 Başlangıç, 4 Orta, 3 İleri)
        val allPool = LearningContent.diagnosticQuestions
        val beginnerCandidates = allPool.filter { it.level == Level.BEGINNER }
        val intermediateCandidates = allPool.filter { it.level == Level.INTERMEDIATE }
        val advancedCandidates = allPool.filter { it.level == Level.ADVANCED }
        
        fun filterByStyle(candidates: List<com.example.lingoscroll.data.LearningItem>): List<com.example.lingoscroll.data.LearningItem> {
            if (style == "MIXED") return candidates
            val styleFiltered = candidates.filter { it.category == style || it.category == "CASUAL" }
            return if (styleFiltered.isNotEmpty()) styleFiltered else candidates
        }
        
        val begPool = filterByStyle(beginnerCandidates).shuffled()
        val intPool = filterByStyle(intermediateCandidates).shuffled()
        val advPool = filterByStyle(advancedCandidates).shuffled()
        
        val selectedBeg = begPool.take(3)
        val selectedInt = intPool.take(4)
        val selectedAdv = advPool.take(3)
        
        val combinedPool = (selectedBeg + selectedInt + selectedAdv).shuffled().map {
            WordCard(
                id = it.id,
                type = it.type.name,
                expression = it.phrase,
                translation = it.translation,
                example_sentence = it.context,
                level = it.level.name,
                optionsRaw = it.options.joinToString("|"),
                correctAnswer = it.correctAnswer,
                category = it.category
            )
        }
        
        activeDiagnosticQuestions = combinedPool
        
        if (combinedPool.isNotEmpty()) {
            val firstQuestion = combinedPool.first()
            _uiState.value = MainScreenUiState.OnboardingQuiz(
                currentQuestionIndex = 0,
                totalQuestions = combinedPool.size,
                currentQuestion = firstQuestion,
                selectedOption = null,
                correctCount = 0
            )
        }
    }

    // Seviye tespit sorusunu cevaplama
    fun answerDiagnosticQuestion(option: String) {
        val currentState = _uiState.value as? MainScreenUiState.OnboardingQuiz ?: return
        val isCorrect = option == currentState.currentQuestion.correctAnswer
        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount

        _uiState.value = currentState.copy(
            selectedOption = option,
            correctCount = newCorrectCount
        )

        tts.speak(getSpeakText(currentState.currentQuestion, true))
    }

    // Seviye tespit sınavında bir sonraki soruya geçiş
    fun nextDiagnosticQuestion() {
        val currentState = _uiState.value as? MainScreenUiState.OnboardingQuiz ?: return
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < activeDiagnosticQuestions.size) {
            val nextCard = activeDiagnosticQuestions[nextIndex]
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                currentQuestion = nextCard,
                selectedOption = null
            )
        } else {
            val score = currentState.correctCount
            val calculatedLevel = when {
                score <= 3 -> Level.BEGINNER
                score <= 7 -> Level.INTERMEDIATE
                else -> Level.ADVANCED
            }
            prefs.setUserLevel(calculatedLevel)
            prefs.updateStreak()
            selectedLevel = calculatedLevel
            
            // Başlangıç aşamasını ayarla (1, 6 veya 11)
            val startStage = when (calculatedLevel) {
                Level.BEGINNER -> 1
                Level.INTERMEDIATE -> 6
                Level.ADVANCED -> 11
            }
            prefs.setCurrentStage(startStage)
            prefs.setStageProgress(0)

            _uiState.value = MainScreenUiState.OnboardingLevelReveal(calculatedLevel, score)
        }
    }

    // Seviye bildirim ekranından çıkıp çalışmaya başlama
    fun startLearningAfterSBS() {
        val level = selectedLevel ?: Level.BEGINNER
        startStageSession(level)
    }

    // Pratik modunda sıradaki soruyu yükler (Session Queue bazlı)
    private fun loadNextPracticeItem(level: Level) {
        if (sessionQueue.isEmpty()) {
            startStageSession(level)
            return
        }

        val chosenItem = sessionQueue.first()
        val variations = chosenItem.variationsList
        val finalItem = if (chosenItem.type == "QUIZ_COMPLETION" && variations.isNotEmpty()) {
            val randomVariation = variations.random()
            val processedVariation = if (!randomVariation.contains("_____") && randomVariation.contains(chosenItem.correctAnswer, ignoreCase = true)) {
                randomVariation.replace(chosenItem.correctAnswer, "_____", ignoreCase = true)
            } else {
                randomVariation
            }

            val phrase = chosenItem.expression
            val end = phrase.lastIndexOf("'")
            if (end != -1) {
                val start = phrase.lastIndexOf("'", end - 1)
                if (start != -1) {
                    val prefix = phrase.substring(0, start + 1)
                    val suffix = phrase.substring(end)
                    chosenItem.copy(expression = prefix + processedVariation + suffix)
                } else {
                    chosenItem.copy(expression = processedVariation)
                }
            } else {
                chosenItem.copy(expression = processedVariation)
            }
        } else {
            chosenItem
        }

        val preparedItem = if (finalItem.optionsRaw.trim().isEmpty() || finalItem.optionsList.isEmpty()) {
            val dist = listOf("time", "work", "day", "world", "life", "place", "home", "back", "show", "call", "money", "check")
                .filter { it.lowercase() != finalItem.correctAnswer.lowercase() }
                .shuffled()
                .take(3)
            val mixed = (dist + finalItem.correctAnswer).shuffled()
            finalItem.copy(optionsRaw = mixed.joinToString("|"))
        } else {
            val shuffledOpts = finalItem.optionsList.shuffled()
            finalItem.copy(optionsRaw = shuffledOpts.joinToString("|"))
        }

        _uiState.value = MainScreenUiState.Practice(
            currentItem = preparedItem,
            isMeaningRevealed = false,
            selectedOption = null,
            isAnswerEvaluated = false,
            isAnswerCorrect = false,
            streak = prefs.getStreak(),
            secondsSaved = prefs.getTotalSecondsSaved(),
            currentLevel = level,
            secondsSpentToday = prefs.getSecondsSpentToday() + secondsTimerActive,
            selectedStyle = prefs.getUserStyle(),
            speechRate = prefs.getTtsSpeechRate(),
            currentStage = prefs.getCurrentStage(),
            stageProgress = prefs.getStageProgress(),
            showStageComplete = false
        )
    }

    // Pratik sorusunu cevaplama
    fun answerPracticeQuestion(option: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return

        val isCorrect = option == currentState.currentItem.correctAnswer
        prefs.incrementQuestionsStats(isCorrect)
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, isCorrect)
        }
        
        if (isCorrect) {
            prefs.addSecondsSaved(20)
        }

        _uiState.value = currentState.copy(
            selectedOption = option,
            isAnswerEvaluated = true,
            isAnswerCorrect = isCorrect,
            secondsSaved = prefs.getTotalSecondsSaved()
        )

        tts.speak(getSpeakText(currentState.currentItem, true))
    }

    // Sıradaki pratik sorusuna geçiş
    fun nextPracticeQuestion() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (!currentState.isAnswerEvaluated) return

        val isCorrect = currentState.isAnswerCorrect

        if (isCorrect) {
            prefs.addRecentSeenId(currentState.currentItem.id)
            if (sessionQueue.isNotEmpty()) {
                sessionQueue.removeAt(0)
            }
            prefs.incrementStageProgress()
            
            if (prefs.getStageProgress() >= 15) {
                _uiState.value = currentState.copy(
                    stageProgress = 15,
                    showStageComplete = true
                )
                return
            }
        } else {
            // Yanlış cevaplanan soruyu pekiştirme için kuyruğun sonuna taşı (Duolingo tarzı)
            if (sessionQueue.isNotEmpty()) {
                val wrongCard = sessionQueue.removeAt(0)
                sessionQueue.add(wrongCard)
            }
        }

        loadNextPracticeItem(currentState.currentLevel)
    }

    // Bir sonraki aşamaya geçiş (Kilit açma)
    fun proceedToNextStage() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        val currentStage = prefs.getCurrentStage()
        val nextStage = currentStage + 1
        
        prefs.setCurrentStage(nextStage)
        prefs.setStageProgress(0)
        
        val calculatedLevel = when {
            nextStage <= 5 -> Level.BEGINNER
            nextStage <= 10 -> Level.INTERMEDIATE
            else -> Level.ADVANCED
        }
        prefs.setUserLevel(calculatedLevel)
        selectedLevel = calculatedLevel
        
        startStageSession(calculatedLevel)
    }

    // Bağımsız Kelime Kartı Çalışma Metotları
    fun openLearningCards() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCardsByLevel(currentState.currentLevel.name).collectLatest { cards ->
                val userStyle = prefs.getUserStyle()
                val filtered = cards.filter { 
                    it.type == "CARD" && (userStyle == "MIXED" || it.category == userStyle)
                }
                
                viewModelScope.launch(Dispatchers.Main) {
                    val cardsToUse = if (filtered.isEmpty()) cards.filter { it.type == "CARD" } else filtered
                    if (cardsToUse.isNotEmpty()) {
                        _uiState.value = currentState.copy(
                            learningCards = cardsToUse,
                            currentLearningCardIndex = 0,
                            showLearningCards = true,
                            isLearningCardRevealed = false
                        )
                        tts.speak(getSpeakText(cardsToUse[0], true))
                    }
                }
            }
        }
    }

    fun closeLearningCards() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        _uiState.value = currentState.copy(showLearningCards = false)
    }

    fun revealLearningCard() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        _uiState.value = currentState.copy(isLearningCardRevealed = true)
        
        val currentCard = currentState.learningCards.getOrNull(currentState.currentLearningCardIndex)
        if (currentCard != null) {
            val phrase = currentCard.expression
            val example = currentCard.example_sentence
            val speakText = if (example.isNotEmpty()) "$phrase. For example: $example" else phrase
            tts.speak(speakText)
        }
    }

    fun nextLearningCard(gotIt: Boolean) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        val nextIndex = currentState.currentLearningCardIndex + 1
        
        val currentCard = currentState.learningCards.getOrNull(currentState.currentLearningCardIndex)
        if (currentCard != null) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateCardProgress(currentCard.id, gotIt)
            }
            if (gotIt) {
                prefs.addSecondsSaved(10)
            }
        }

        if (nextIndex < currentState.learningCards.size) {
            _uiState.value = currentState.copy(
                currentLearningCardIndex = nextIndex,
                isLearningCardRevealed = false
            )
            val nextCard = currentState.learningCards[nextIndex]
            tts.speak(getSpeakText(nextCard, true))
        } else {
            _uiState.value = currentState.copy(
                showLearningCards = false,
                secondsSaved = prefs.getTotalSecondsSaved()
            )
        }
    }

    // --- Anlık Arka Planda Senkronizasyon (Immediate Foreground Sync) ---
    fun triggerForegroundSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL(prefs.getCustomFeedUrl())
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    conn.disconnect()

                    val jsonArray = JSONArray(response.toString())
                    val newCards = mutableListOf<WordCard>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val optionsArray = obj.optJSONArray("options")
                        val optionsList = mutableListOf<String>()
                        if (optionsArray != null) {
                            for (j in 0 until optionsArray.length()) {
                                optionsList.add(optionsArray.getString(j))
                            }
                        }
                        val variationsArray = obj.optJSONArray("variations")
                        val variationsList = mutableListOf<String>()
                        if (variationsArray != null) {
                            for (j in 0 until variationsArray.length()) {
                                variationsList.add(variationsArray.getString(j))
                            }
                        }

                        newCards.add(
                            WordCard(
                                id = obj.getInt("id"),
                                type = obj.getString("type"),
                                level = obj.getString("level"),
                                expression = obj.getString("phrase"),
                                translation = obj.getString("translation"),
                                example_sentence = obj.optString("context", ""),
                                optionsRaw = optionsList.joinToString("|"),
                                correctAnswer = obj.getString("correctAnswer"),
                                category = obj.optString("category", "CASUAL"),
                                variationsRaw = variationsList.joinToString("||")
                            )
                        )
                    }

                    if (newCards.isNotEmpty()) {
                        repository.insertCards(newCards)
                        Log.d("MainScreenViewModel", "Foreground Sync Başarılı: ${newCards.size} yeni gündem sorusu eklendi.")
                    }
                }
            } catch (e: Exception) {
                // Simülasyon: 1.5 saniye sonra güncel Apple etkinliği, havalimanı ve orman yangını sorularını ekle
                kotlinx.coroutines.delay(1500)
                val simulatedCards = listOf(
                    WordCard(
                        id = 1001,
                        type = "QUIZ_COMPLETION",
                        level = "BEGINNER",
                        expression = "Havalimanında bilet kontrolü sırasında: 'Please show me your _____.'",
                        translation = "ticket",
                        example_sentence = "'Please show me your...' seyahat sırasında en çok duyacağınız kalıplardandır.",
                        correctAnswer = "ticket",
                        optionsRaw = "ticket|bag|car|room",
                        category = "TRAVEL",
                        variationsRaw = "Excuse me, please show me your _____ || May I see your _____ please?"
                    ),
                    WordCard(
                        id = 1002,
                        type = "QUIZ_MULTIPLE_CHOICE",
                        level = "INTERMEDIATE",
                        expression = "Apple yeni gözlüğünü tanıttı! Tanıtımda geçen 'Son teknoloji / Çığır açıcı' anlamına gelen terim hangisidir?",
                        translation = "State of the art",
                        example_sentence = "Teknolojide en son gelişmişlik seviyesini tanımlamak için kullanılır.",
                        correctAnswer = "State of the art",
                        optionsRaw = "State of the art|Old school|Low key|Over the top",
                        category = "BUSINESS"
                    ),
                    WordCard(
                        id = 1003,
                        type = "QUIZ_COMPLETION",
                        level = "ADVANCED",
                        expression = "Orman yangını haberlerinde geçen 'Tahliye etmek / Güvenli yere almak' anlamındaki kelime: 'Locals had to _____ the area.'",
                        translation = "evacuate",
                        example_sentence = "'Evacuate', acil durumlarda bölgeyi boşaltmak anlamındadır.",
                        correctAnswer = "evacuate",
                        optionsRaw = "evacuate|stay|burn|hide",
                        category = "MIXED",
                        variationsRaw = "The police ordered residents to _____ the building || Everyone had to _____ because of the fire."
                    )
                )
                repository.insertCards(simulatedCards)
            }
        }
    }
    fun revealCardMeaning() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        _uiState.value = currentState.copy(isMeaningRevealed = true)
        tts.speak(getSpeakText(currentState.currentItem, true))
    }

    fun evaluateCard(gotIt: Boolean) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, gotIt)
        }
        if (gotIt) {
            prefs.addSecondsSaved(10)
        }
        loadNextPracticeItem(currentState.currentLevel)
    }

    fun changeLearningStyle(newStyle: String) {
        prefs.setUserStyle(newStyle)
        val currentState = _uiState.value as? MainScreenUiState.Practice
        if (currentState != null) {
            prefs.setStageProgress(0)
            startStageSession(currentState.currentLevel)
        }
    }

    private fun scheduleBackgroundSync(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
            .build()

        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.lingoscroll.sync.SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LingoScrollSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun scheduleNotifications(context: Context) {
        val notificationRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.lingoscroll.sync.NotificationWorker>(12, java.util.concurrent.TimeUnit.HOURS)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LingoScrollNotifications",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            notificationRequest
        )
    }

    private fun startActiveTimer() {
        viewModelScope.launch {
            while (coroutineContext[kotlinx.coroutines.Job]?.isActive == true) {
                kotlinx.coroutines.delay(1000)
                secondsTimerActive += 1
                
                if (secondsTimerActive >= 15L) {
                    prefs.addSecondsSpentToday(secondsTimerActive)
                    secondsTimerActive = 0L
                }
                
                val state = _uiState.value
                if (state is MainScreenUiState.Practice) {
                    _uiState.value = state.copy(
                        secondsSpentToday = prefs.getSecondsSpentToday() + secondsTimerActive
                    )
                }
            }
        }
    }

    fun speakText(text: String) {
        if (text.isEmpty()) {
            val currentState = _uiState.value as? MainScreenUiState.Practice
            if (currentState != null) {
                tts.speak(getSpeakText(currentState.currentItem, currentState.isAnswerEvaluated))
            }
        } else {
            tts.speak(text)
        }
    }

    fun resetProgress() {
        prefs.clearUserProgress()
        prefs.clearRecentSeenIds()
        pendingCardUpdates.clear()
        sessionQueue.clear()
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
        
        _uiState.value = MainScreenUiState.OnboardingWelcome
    }

    override fun onCleared() {
        saveProgress()
        tts.shutdown()
        super.onCleared()
    }

    fun changeTtsSpeechRate(rate: Float) {
        prefs.setTtsSpeechRate(rate)
        tts.setSpeechRate(rate)
    }

    fun updateFeedUrl(newUrl: String) {
        prefs.setCustomFeedUrl(newUrl)
        triggerForegroundSync()
    }

    fun getCustomFeedUrl(): String {
        return prefs.getCustomFeedUrl()
    }

    fun getStudyStats(): StudyStats {
        val total = prefs.getTotalQuestionsAnswered()
        val correct = prefs.getCorrectQuestionsAnswered()
        val accuracy = if (total > 0) (correct * 100) / total else 0
        return StudyStats(
            streak = prefs.getStreak(),
            totalMinutes = (prefs.getSecondsSpentToday() + secondsTimerActive) / 60,
            totalAnswered = total,
            correctAnswered = correct,
            accuracyPercent = accuracy
        )
    }
}

data class StudyStats(
    val streak: Int,
    val totalMinutes: Long,
    val totalAnswered: Int,
    val correctAnswered: Int,
    val accuracyPercent: Int
)
