package com.example.lingoscroll.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingoscroll.data.local.AppDatabase
import com.example.lingoscroll.data.local.SurvivalCard
import com.example.lingoscroll.data.Level
import com.example.lingoscroll.data.PreferencesManager
import com.example.lingoscroll.data.repository.CardRepository
import com.example.lingoscroll.data.tts.NativeTtsManager
import com.example.lingoscroll.data.tts.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray

sealed interface MainScreenUiState {
    object OnboardingWelcome : MainScreenUiState
    
    data class OnboardingQuiz(
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val currentQuestion: SurvivalCard,
        val selectedOption: String?,
        val correctCount: Int,
        val shuffledOptions: List<String> = emptyList()
    ) : MainScreenUiState

    data class OnboardingLevelReveal(
        val categoryFocus: String,
        val startingStage: Int,
        val correctCount: Int
    ) : MainScreenUiState

    data class Practice(
        val currentItem: SurvivalCard,
        val isMeaningRevealed: Boolean = false,
        val selectedOption: String? = null,
        val isAnswerEvaluated: Boolean = false,
        val isAnswerCorrect: Boolean = false,
        val streak: Int,
        val secondsSaved: Long,
        val currentCategory: String, // CRISIS, NAVIGATION, FINANCE, BASIC_NEEDS, MIXED
        val secondsSpentToday: Long = 0L,
        val currentStage: Int = 1,
        val stageProgress: Int = 0,
        val showStageComplete: Boolean = false,
        
        // Skeleton mechanic fields
        val isSkeletonRevealed: Boolean = false,
        // Chunk mechanic fields
        val clickedChunks: List<String> = emptyList(),
        val shuffledChunks: List<String> = emptyList(),
        // Error find mechanic fields
        val tappedErrorWord: String? = null,
        val errorSentenceText: String = "",
        val isErrorCorrected: Boolean = false,
        
        val shuffledOptions: List<String> = emptyList(),
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

    private val pendingCardUpdates = mutableMapOf<Int, Boolean>()
    private val failCountMap = mutableMapOf<Int, Int>()
    private val sessionQueue = mutableListOf<SurvivalCard>()
    private var secondsTimerActive = 0L

    private fun updateFailCount(cardId: Int, isCorrect: Boolean) {
        if (isCorrect) {
            failCountMap.remove(cardId)
        } else {
            val current = failCountMap[cardId] ?: 0
            failCountMap[cardId] = current + 1
        }
    }
    private var activeStressTestQuestions = listOf<SurvivalCard>()

    // 5 Soruluk Statik Onboarding Stres Testi Havuzu
    private val stressTestQuestions = listOf(
        SurvivalCard(
            id = 9901,
            category = "CRISIS",
            mechanicType = "SWIPE",
            scenarioTr = "Taksici taksimetreyi açmayı reddediyor ve fahiş fiyat istiyor. Doğru tepki nedir?",
            targetEn = "Turn on the meter, please.",
            optionsRaw = "Turn on the meter, please.|Okay, I will pay whatever.",
            difficulty = 2
        ),
        SurvivalCard(
            id = 9902,
            category = "NAVIGATION",
            mechanicType = "SWIPE",
            scenarioTr = "Kayboldun ve en yakın metro istasyonunu sorman gerek.",
            targetEn = "Where is the nearest subway station?",
            optionsRaw = "Where is the nearest subway station?|Do you have coffee?",
            difficulty = 1
        ),
        SurvivalCard(
            id = 9903,
            category = "FINANCE",
            mechanicType = "SWIPE",
            scenarioTr = "Hesabı nakit yerine kredi kartıyla ödemek istediğini söyle.",
            targetEn = "Can I pay by credit card?",
            optionsRaw = "Can I pay by credit card?|Can you give me a discount?",
            difficulty = 2
        ),
        SurvivalCard(
            id = 9904,
            category = "BASIC_NEEDS",
            mechanicType = "SWIPE",
            scenarioTr = "Şarj aletin için acil priz dönüştürücüye ihtiyacın var.",
            targetEn = "Do you have a plug adapter?",
            optionsRaw = "Do you have a plug adapter?|I need an ambulance.",
            difficulty = 3
        ),
        SurvivalCard(
            id = 9905,
            category = "CRISIS",
            mechanicType = "SWIPE",
            scenarioTr = "Pasaportunu kaybettiğini konsolosluğa bildirmelisin.",
            targetEn = "I lost my passport.",
            optionsRaw = "I lost my passport.|Where is the elevator?",
            difficulty = 2
        )
    )

    init {
        val currentBuildTime = "20260702_0000"
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
            prefs.updateStreak()
            startStageSession(prefs.getUserStyle())
        } else {
            _uiState.value = MainScreenUiState.OnboardingWelcome
        }
    }

    // Aşama Oturum Başlatıcı (9 Yeni + 6 Tekrar)
    fun startStageSession(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exclude = prefs.getRecentSeenIds()
            val questions = repository.getStagePackage(category, exclude, System.currentTimeMillis())
            
            viewModelScope.launch(Dispatchers.Main) {
                sessionQueue.clear()
                sessionQueue.addAll(questions)
                loadNextPracticeItem(category)
            }
        }
    }

    // İlerlemeyi Room veritabanına kaydetme
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

    // Survival Stress Testini Başlatma
    fun startStressTest() {
        activeStressTestQuestions = stressTestQuestions.shuffled()
        if (activeStressTestQuestions.isNotEmpty()) {
            val firstQuestion = activeStressTestQuestions.first()
            _uiState.value = MainScreenUiState.OnboardingQuiz(
                currentQuestionIndex = 0,
                totalQuestions = activeStressTestQuestions.size,
                currentQuestion = firstQuestion,
                selectedOption = null,
                correctCount = 0,
                shuffledOptions = firstQuestion.optionsList.shuffled()
            )
        }
    }

    // Stress Test Cevaplama
    fun answerStressTestQuestion(option: String) {
        val currentState = _uiState.value as? MainScreenUiState.OnboardingQuiz ?: return
        val isCorrect = option == currentState.currentQuestion.targetEn
        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount

        _uiState.value = currentState.copy(
            selectedOption = option,
            correctCount = newCorrectCount
        )

        tts.speak(currentState.currentQuestion.targetEn)
    }

    // Stress Test Sonraki Soru
    fun nextStressTestQuestion() {
        val currentState = _uiState.value as? MainScreenUiState.OnboardingQuiz ?: return
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < activeStressTestQuestions.size) {
            val nextCard = activeStressTestQuestions[nextIndex]
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                currentQuestion = nextCard,
                selectedOption = null,
                shuffledOptions = nextCard.optionsList.shuffled()
            )
        } else {
            val score = currentState.correctCount
            
            // Sonuca göre başlangıç kategorisi ve aşaması belirleme
            val (categoryFocus, startStage) = when {
                score <= 2 -> Pair("CRISIS", 1)         // Kriz durumları öncelikli (Aşama 1)
                score <= 4 -> Pair("FINANCE", 6)        // Finans / Hesap öncelikli (Aşama 6)
                else -> Pair("NAVIGATION", 11)          // Navigasyon / Yol bulma (Aşama 11)
            }

            prefs.setUserStyle(categoryFocus)
            prefs.setUserLevel(Level.BEGINNER) // dummy level for lifecycle compilation
            prefs.updateStreak()
            prefs.setCurrentStage(startStage)
            prefs.setStageProgress(0)

            _uiState.value = MainScreenUiState.OnboardingLevelReveal(
                categoryFocus = categoryFocus,
                startingStage = startStage,
                correctCount = score
            )
        }
    }

    // Test Sonrası Eğitime Başla
    fun startLearningAfterStressTest() {
        val category = prefs.getUserStyle()
        startStageSession(category)
    }

    // Yeni Soru Yükleyici
    private fun loadNextPracticeItem(category: String) {
        if (sessionQueue.isEmpty()) {
            startStageSession(category)
            return
        }

        val chosenItem = sessionQueue.first()
        
        // CHUNK mekaniği için parçaları oluştur
        val clickedChunks = emptyList<String>()
        val shuffledChunks = if (chosenItem.mechanicType == "CHUNK") {
            chosenItem.optionsList.shuffled()
        } else {
            emptyList()
        }

        // ERROR_FIND mekaniği için kelimeleri ve metni hazırla
        val errorSentenceText = if (chosenItem.mechanicType == "ERROR_FIND") {
            chosenItem.optionsList.getOrNull(0) ?: ""
        } else {
            ""
        }

        val shuffledOpts = chosenItem.optionsList.shuffled()

        _uiState.value = MainScreenUiState.Practice(
            currentItem = chosenItem,
            isMeaningRevealed = false,
            selectedOption = null,
            isAnswerEvaluated = false,
            isAnswerCorrect = false,
            streak = prefs.getStreak(),
            secondsSaved = prefs.getTotalSecondsSaved(),
            currentCategory = category,
            secondsSpentToday = prefs.getSecondsSpentToday() + secondsTimerActive,
            selectedStyle = prefs.getUserStyle(),
            speechRate = prefs.getTtsSpeechRate(),
            currentStage = prefs.getCurrentStage(),
            stageProgress = prefs.getStageProgress(),
            showStageComplete = false,
            
            isSkeletonRevealed = false,
            clickedChunks = clickedChunks,
            shuffledChunks = shuffledChunks,
            tappedErrorWord = null,
            errorSentenceText = errorSentenceText,
            isErrorCorrected = false,
            shuffledOptions = shuffledOpts
        )
    }

    // Soru Cevaplama
    fun answerPracticeQuestion(answer: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return

        val isCorrect = answer == currentState.currentItem.targetEn
        prefs.incrementQuestionsStats(isCorrect)
        updateFailCount(currentState.currentItem.id, isCorrect)
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, isCorrect)
        }
        
        if (isCorrect) {
            prefs.addSecondsSaved(20)
        }

        _uiState.value = currentState.copy(
            selectedOption = answer,
            isAnswerEvaluated = true,
            isAnswerCorrect = isCorrect,
            secondsSaved = prefs.getTotalSecondsSaved()
        )

        tts.speak(currentState.currentItem.targetEn)
    }

    // Skeleton İçin Kendi Kendini Değerlendirme (Kolay/Zor/Bilemedim)
    fun evaluateSkeleton(success: Boolean) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (!currentState.isSkeletonRevealed) return

        prefs.incrementQuestionsStats(success)
        updateFailCount(currentState.currentItem.id, success)

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, success)
        }

        if (success) {
            prefs.addSecondsSaved(15)
        }

        _uiState.value = currentState.copy(
            isAnswerEvaluated = true,
            isAnswerCorrect = success,
            secondsSaved = prefs.getTotalSecondsSaved()
        )

        tts.speak(currentState.currentItem.targetEn)
    }

    // Skeleton Hedefi Açma
    fun revealSkeleton() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        _uiState.value = currentState.copy(isSkeletonRevealed = true)
        tts.speak(currentState.currentItem.targetEn)
    }

    // Chunk Butonuna Tıklama
    fun clickChunk(chunk: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return

        val newClicked = currentState.clickedChunks.toMutableList()
        if (chunk in newClicked) {
            newClicked.remove(chunk)
        } else {
            newClicked.add(chunk)
        }

        // Derhal selectedChunks (clickedChunks) listesine ekle ve UI güncellemesine izin ver
        _uiState.value = currentState.copy(clickedChunks = newClicked)

        // Bütün parçalar seçildi mi kontrol et
        if (newClicked.size == currentState.currentItem.optionsList.size) {
            viewModelScope.launch(Dispatchers.Main) {
                kotlinx.coroutines.delay(300L)
                
                // Güvenlik: Kullanıcının seçtiği blok sayısı, toplam blok sayısına hala eşit mi ve değerlendirilmedi mi?
                val latestState = _uiState.value as? MainScreenUiState.Practice ?: return@launch
                if (latestState.clickedChunks.size == latestState.currentItem.optionsList.size && !latestState.isAnswerEvaluated) {
                    val isCorrect = latestState.clickedChunks == latestState.currentItem.optionsList
                    prefs.incrementQuestionsStats(isCorrect)
                    updateFailCount(latestState.currentItem.id, isCorrect)
                    
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updateCardProgress(latestState.currentItem.id, isCorrect)
                    }
                    
                    if (isCorrect) {
                        prefs.addSecondsSaved(20)
                    }

                    _uiState.value = latestState.copy(
                        clickedChunks = latestState.clickedChunks,
                        isAnswerEvaluated = true,
                        isAnswerCorrect = isCorrect,
                        secondsSaved = prefs.getTotalSecondsSaved()
                    )

                    tts.speak(latestState.currentItem.targetEn)
                }
            }
        }
    }

    // Seçilen blokları tamamen sıfırlama (baştan dizmek için)
    fun clearClickedChunks() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return
        _uiState.value = currentState.copy(clickedChunks = emptyList())
    }

    // Error Find Kelimesine Tıklama
    fun clickErrorWord(word: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return

        val wrongWord = currentState.currentItem.optionsList.getOrNull(1) ?: ""
        val correctReplacement = currentState.currentItem.optionsList.getOrNull(2) ?: ""

        val cleanClicked = word.lowercase().replace(Regex("[.,!?]"), "").trim()
        val cleanTarget = currentState.currentItem.targetEn.lowercase().replace(Regex("[.,!?]"), "")
        val targetWords = cleanTarget.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

        // Kural: Eğer temizlenmiş clickedWord, temizlenmiş targetEn cümlesinin kelimeleri içinde YOKSA başarılı say
        val isCorrect = cleanClicked !in targetWords
        prefs.incrementQuestionsStats(isCorrect)
        updateFailCount(currentState.currentItem.id, isCorrect)

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, isCorrect)
        }

        if (isCorrect) {
            prefs.addSecondsSaved(20)
            // Yanlış kelimeyi doğrusu ile değiştir
            val correctedText = currentState.errorSentenceText.replace(wrongWord, correctReplacement, ignoreCase = true)
            _uiState.value = currentState.copy(
                tappedErrorWord = word,
                isAnswerEvaluated = true,
                isAnswerCorrect = true,
                errorSentenceText = correctedText,
                isErrorCorrected = true,
                secondsSaved = prefs.getTotalSecondsSaved()
            )
        } else {
            _uiState.value = currentState.copy(
                tappedErrorWord = word,
                isAnswerEvaluated = true,
                isAnswerCorrect = false,
                isErrorCorrected = false
            )
        }

        tts.speak(currentState.currentItem.targetEn)
    }

    // Sıradaki Soruya Geçiş
    fun nextPracticeQuestion() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (!currentState.isAnswerEvaluated) return

        val isCorrect = currentState.isAnswerCorrect
        val cardId = currentState.currentItem.id

        if (isCorrect) {
            failCountMap.remove(cardId)
            prefs.addRecentSeenId(cardId)
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
            val currentFailCount = failCountMap[cardId] ?: 0
            if (currentFailCount >= 3) {
                // 3-Strike Rule: Üst üste 3 kez yanlış bilindiğinde force pass uygula
                failCountMap.remove(cardId)
                prefs.addRecentSeenId(cardId)
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
                // Yanlış yapılan soruyu normal olarak kuyruğun sonuna taşı
                if (sessionQueue.isNotEmpty()) {
                    val wrongCard = sessionQueue.removeAt(0)
                    sessionQueue.add(wrongCard)
                }
            }
        }

        loadNextPracticeItem(currentState.currentCategory)
    }

    // Sonraki Aşamaya Geçiş
    fun proceedToNextStage() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        val currentStage = prefs.getCurrentStage()
        val nextStage = currentStage + 1
        
        prefs.setCurrentStage(nextStage)
        prefs.setStageProgress(0)
        
        startStageSession(currentState.currentCategory)
    }

    // Kategori Seçimini Değiştirme (Acil Durum Alt Menüsü İçin)
    fun changeCategory(newCategory: String) {
        prefs.setUserStyle(newCategory)
        prefs.setStageProgress(0)
        startStageSession(newCategory)
    }

    // --- Anlık Arka Planda Senkronizasyon ---
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
                    val newCards = mutableListOf<SurvivalCard>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val optionsArray = obj.optJSONArray("options")
                        val optionsList = mutableListOf<String>()
                        if (optionsArray != null) {
                            for (j in 0 until optionsArray.length()) {
                                optionsList.add(optionsArray.getString(j))
                            }
                        }

                        newCards.add(
                            SurvivalCard(
                                id = obj.getInt("id"),
                                category = obj.getString("category"),
                                mechanicType = obj.getString("mechanicType"),
                                scenarioTr = obj.getString("scenarioTr"),
                                targetEn = obj.getString("targetEn"),
                                optionsRaw = optionsList.joinToString("|"),
                                difficulty = obj.optInt("difficulty", 3),
                                nextReviewDate = 0L
                            )
                        )
                    }

                    if (newCards.isNotEmpty()) {
                        repository.insertCards(newCards)
                        Log.d("MainScreenViewModel", "Foreground Sync Başarılı: ${newCards.size} yeni acil durum kartı eklendi.")
                    }
                }
            } catch (e: Exception) {
                // Fallback or debug logs
            }
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
                tts.speak(currentState.currentItem.targetEn)
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
