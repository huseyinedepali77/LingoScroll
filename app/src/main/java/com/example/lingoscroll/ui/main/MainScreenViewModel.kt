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

    data class Practice(
        val currentItem: WordCard,
        val isMeaningRevealed: Boolean = false, // CARD tipi için (artık kullanılmıyor ama uyumluluk için kaldı)
        val selectedOption: String? = null,    // QUIZ tipi için
        val isAnswerEvaluated: Boolean = false, // QUIZ tipi için
        val isAnswerCorrect: Boolean = false,    // QUIZ tipi için
        val streak: Int,
        val secondsSaved: Long,
        val currentLevel: Level,
        val secondsSpentToday: Long = 0L,
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
    
    // TtsManager soyutlamasını kullanıyoruz ve native TTS motorunu enjekte ediyoruz
    private val tts: TtsManager = NativeTtsManager(context)

    // Room Database ve Repository kurulumu
    private val db = AppDatabase.getDatabase(context, viewModelScope)
    private val repository = CardRepository(db.cardDao())

    private val _uiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.OnboardingWelcome)
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    // RAM'de tutulan geçici ilerleme verileri (UI takılmalarını önlemek için)
    private val pendingCardUpdates = mutableMapOf<Int, Boolean>() // cardId -> isCorrect
    
    private var lastSeenItemId: Int? = null
    private var allLevelCards = listOf<WordCard>()
    private var selectedLevel: Level? = null
    private var secondsTimerActive = 0L
    private var activeDiagnosticQuestions = listOf<WordCard>()

    // Sadece İngilizce kısımları ve tamamlanmış cümleleri okumak için yardımcı fonksiyon (Cevap verilmeden önce cevabı sızdırmaz)
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
                    card.correctAnswer
                } else {
                    // Soru cümlesini oku (Türkçe karakter içermiyorsa)
                    if (card.expression.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                        card.expression
                    } else {
                        "Please choose the correct answer"
                    }
                }
            }
            else -> {
                card.expression
            }
        }
    }

    init {
        // Her yeni yüklemede seviye tespit sınavının (SBS) baştan gelmesini sağlamak için build zamanı kontrolü
        val currentBuildTime = "20260701_1539" 
        val savedBuildTime = prefs.getSavedBuildTime()
        if (savedBuildTime != currentBuildTime) {
            prefs.clearUserProgress()
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteAll()
                // Veritabanını baştan kur ve statik verileri ekle (SQLite dosyasının yeniden oluşmasını beklemeden)
                val diagCards = LearningContent.diagnosticQuestions.map {
                    WordCard(
                        id = it.id,
                        type = it.type.name,
                        expression = it.phrase,
                        translation = it.translation,
                        example_sentence = it.context,
                        level = it.level.name,
                        optionsRaw = it.options.joinToString("|"),
                        correctAnswer = it.correctAnswer,
                        category = it.category,
                        variationsRaw = it.variations.joinToString("||")
                    )
                }
                val practiceCards = LearningContent.practiceItems.map {
                    WordCard(
                        id = it.id,
                        type = it.type.name,
                        expression = it.phrase,
                        translation = it.translation,
                        example_sentence = it.context,
                        level = it.level.name,
                        optionsRaw = it.options.joinToString("|"),
                        correctAnswer = it.correctAnswer,
                        category = it.category,
                        variationsRaw = it.variations.joinToString("||")
                    )
                }
                repository.insertCards(diagCards)
                repository.insertCards(practiceCards)
            }
            prefs.setSavedBuildTime(currentBuildTime)
        }

        // TTS hızını SharedPreferences'taki değere göre ayarla
        tts.setSpeechRate(prefs.getTtsSpeechRate())

        // Anlık Arka Plan Senkronizasyonu (Foreground Sync) Başlat
        triggerForegroundSync()

        // WorkManager background sync planlama (Sadece Wi-Fi ile çalışır)
        scheduleBackgroundSync(context)

        // Akıllı Bildirimleri Planla
        scheduleNotifications(context)

        // Zaman Sayacını Başlat
        startActiveTimer()

        val savedLevel = prefs.getUserLevel()
        if (savedLevel != null) {
            selectedLevel = savedLevel
            prefs.updateStreak()
            observeRoomCards(savedLevel)
        } else {
            _uiState.value = MainScreenUiState.OnboardingWelcome
        }
    }



    // Room Veritabanındaki kartları gerçek zamanlı dinler (Flow)
    private fun observeRoomCards(level: Level) {
        viewModelScope.launch {
            repository.getCardsByLevel(level.name).collectLatest { cards ->
                allLevelCards = cards
                // Eğer Room verileri henüz yüklenmediyse yerel statik bellekten beslen
                if (allLevelCards.isEmpty()) {
                    allLevelCards = LearningContent.practiceItems.filter { it.level == level }.map {
                        WordCard(
                            id = it.id,
                            type = it.type.name,
                            expression = it.phrase,
                            translation = it.translation,
                            example_sentence = it.context,
                            level = it.level.name,
                            optionsRaw = it.options.joinToString("|"),
                            correctAnswer = it.correctAnswer,
                            category = it.category,
                            variationsRaw = it.variations.joinToString("||")
                        )
                    }
                }
                
                // Eğer aktif olarak bir kart gösterilmiyorsa sıradakini yükle
                if (_uiState.value !is MainScreenUiState.Practice) {
                    loadNextPracticeItem(level)
                } else {
                    // Veritabanı değiştikçe feed'i de reaktif güncelle
                    val currentState = _uiState.value as MainScreenUiState.Practice
                    if (allLevelCards.none { it.id == currentState.currentItem.id }) {
                        loadNextPracticeItem(level)
                    }
                }
            }
        }
    }

    // WorkManager Eşitleme Yapılandırması
    private fun scheduleBackgroundSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Sadece Wi-Fi
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LingoScrollSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    // Yaşam Döngüsü tetiklendiğinde (onPause/onStop) RAM'deki verileri Room'a kaydetme
    fun saveProgress() {
        if (secondsTimerActive > 0L) {
            prefs.addSecondsSpentToday(secondsTimerActive)
            secondsTimerActive = 0L
        }

        val updates = pendingCardUpdates.toMap()
        pendingCardUpdates.clear() // Çift yazmayı engellemek için RAM havuzunu hemen boşalt
        
        if (updates.isNotEmpty()) {
            Log.d("MainScreenViewModel", "Yaşam döngüsü tetiklendi: ${updates.size} adet kart ilerlemesi Room veritabanına yazılıyor...")
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

    // Seçilen tarzı kaydeder ve seviye tespit sınavını başlatır
    fun selectLearningStyle(style: String) {
        prefs.setUserStyle(style)
        
        // 10 soruluk dinamik SBS listesini oluştur (3 Başlangıç, 4 Orta, 3 İleri)
        val allPool = LearningContent.diagnosticQuestions
        
        // Seviyelere göre filtrele
        val beginnerCandidates = allPool.filter { it.level == Level.BEGINNER }
        val intermediateCandidates = allPool.filter { it.level == Level.INTERMEDIATE }
        val advancedCandidates = allPool.filter { it.level == Level.ADVANCED }
        
        // Seçilen tarzla eşleşen veya CASUAL olanları filtrele (tarz MIXED değilse)
        fun filterByStyle(candidates: List<com.example.lingoscroll.data.LearningItem>): List<com.example.lingoscroll.data.LearningItem> {
            if (style == "MIXED") return candidates
            val styleFiltered = candidates.filter { it.category == style || it.category == "CASUAL" }
            return if (styleFiltered.isNotEmpty()) styleFiltered else candidates
        }
        
        val begPool = filterByStyle(beginnerCandidates).shuffled()
        val intPool = filterByStyle(intermediateCandidates).shuffled()
        val advPool = filterByStyle(advancedCandidates).shuffled()
        
        // 3 Beginner, 4 Intermediate, 3 Advanced seç
        val selectedBeg = begPool.take(3)
        val selectedInt = intPool.take(4)
        val selectedAdv = advPool.take(3)
        
        // Birleştir ve karıştır (Toplam 10 soru)
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
        
        // Sınavı başlat
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

        // Doğru cevabı / cümleyi İngilizce seslendir
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
            // Sınav bitti, seviyeyi hesapla ve kaydet
            val score = currentState.correctCount
            val calculatedLevel = when {
                score <= 3 -> Level.BEGINNER
                score <= 7 -> Level.INTERMEDIATE
                else -> Level.ADVANCED
            }
            prefs.setUserLevel(calculatedLevel)
            prefs.updateStreak()
            selectedLevel = calculatedLevel
            observeRoomCards(calculatedLevel)
        }
    }

    // Pratik modunda sıradaki soruyu yükler (Spaced Repetition due-date algorithm)
    private fun loadNextPracticeItem(level: Level) {
        if (allLevelCards.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        
        // Kullanıcının seçtiği kategoriye göre filtrele (Seyahat, İş vb.)
        val userStyle = prefs.getUserStyle()
        val categoryFiltered = allLevelCards.filter { 
            userStyle == "MIXED" || it.category == userStyle
        }
        // Eğer seçilen tarza göre havuzda yeterli soru yoksa (4'ten az ise) genel karışık (MIXED) havuzdan beslen
        val targetPool = if (categoryFiltered.size >= 4) categoryFiltered else allLevelCards

        // Sadece QUIZ tiplerini pratik akışına al (CARD tiplerini ayırıyoruz), seviye tespit (SBS) sorularını hariç tut (ID 100-199) ve YKS benzeri uzun paragrafları ele (max 18 kelime)
        val quizCards = targetPool.filter { it.type != "CARD" && it.id !in 100..199 && it.expression.split(" ").size <= 18 }
        if (quizCards.isEmpty()) {
            // Eğer kategoride quiz yoksa genel havuzdan quiz çek (SBS soruları hariç, max 18 kelime)
            val fallbackQuizzes = allLevelCards.filter { it.type != "CARD" && it.id !in 100..199 && it.expression.split(" ").size <= 18 }
            if (fallbackQuizzes.isEmpty()) return
            selectAndEmitQuiz(fallbackQuizzes, level, currentTime)
            return
        }

        selectAndEmitQuiz(quizCards, level, currentTime)
    }

    private fun selectAndEmitQuiz(pool: List<WordCard>, level: Level, currentTime: Long) {
        // Tekrar tarihi gelmiş veya geçmiş kartları filtrele (Öncelikli)
        val dueCards = pool.filter { it.next_review_date <= currentTime }
        val candidates = if (dueCards.isNotEmpty()) dueCards else pool

        // Son görülen sorudan farklı bir soru seçmeye çalış
        var chosenItem = candidates.random()
        if (candidates.size > 1 && chosenItem.id == lastSeenItemId) {
            val otherItems = candidates.filter { it.id != lastSeenItemId }
            chosenItem = otherItems.random()
        }

        // Varyasyon Motoru (Context Variation Engine)
        val variations = chosenItem.variationsList
        val finalItem = if (chosenItem.type == "QUIZ_COMPLETION" && variations.isNotEmpty()) {
            val randomVariation = variations.random()
            // Kalıbın Türkçe açıklama kısmını bozmamak için tırnak içindeki İngilizce cümleyi varyasyon ile değiştiriyoruz
            val phrase = chosenItem.expression
            val end = phrase.lastIndexOf("'")
            if (end != -1) {
                val start = phrase.lastIndexOf("'", end - 1)
                if (start != -1) {
                    val prefix = phrase.substring(0, start + 1)
                    val suffix = phrase.substring(end)
                    chosenItem.copy(expression = prefix + randomVariation + suffix)
                } else {
                    chosenItem.copy(expression = randomVariation)
                }
            } else {
                chosenItem.copy(expression = randomVariation)
            }
        } else {
            chosenItem
        }

        lastSeenItemId = chosenItem.id

        // Seçenekleri Karıştır (Shuffle Options) - Boş ise dinamik olarak doldur (Kullanıcının kilitlenmesini önlemek için)
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
            speechRate = prefs.getTtsSpeechRate()
        )
    }

    // Pratik sorusunu cevaplama
    fun answerPracticeQuestion(option: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return

        val isCorrect = option == currentState.currentItem.correctAnswer
        
        // İstatistikleri güncelle
        prefs.incrementQuestionsStats(isCorrect)
        
        // Veritabanına anında yaz (Tekrarları önlemek için hemen güncelliyoruz)
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

        // Doğru telaffuzu / tamamlanmış İngilizce cümleyi oku
        tts.speak(getSpeakText(currentState.currentItem, true))
    }

    // Kelime kartı değerlendirmesi (Kart okundu / anlaşıldı / tekrar edilecek)
    fun evaluateCard(gotIt: Boolean) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        
        // Veritabanına anında yaz
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardProgress(currentState.currentItem.id, gotIt)
        }
        
        if (gotIt) {
            prefs.addSecondsSaved(10)
        }
        
        loadNextPracticeItem(currentState.currentLevel)
    }

    // Kartın anlamını göster
    fun revealCardMeaning() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        _uiState.value = currentState.copy(isMeaningRevealed = true)

        // Kart açıldığında sadece İngilizce deyimi oku
        tts.speak(getSpeakText(currentState.currentItem, true))
    }

    // --- Bağımsız Kelime Kartı Çalışma Metotları ---
    fun openLearningCards() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        
        val userStyle = prefs.getUserStyle()
        val cards = allLevelCards.filter { 
            it.type == "CARD" && (userStyle == "MIXED" || it.category == userStyle)
        }
        
        if (cards.isEmpty()) {
            val fallbackCards = allLevelCards.filter { it.type == "CARD" }
            if (fallbackCards.isEmpty()) {
                // Eğer seviyede kart yoksa statik hafızadaki tüm kartları yükle (Emniyet supabı)
                val globalCards = LearningContent.practiceItems.filter { it.type == ItemType.CARD }.map {
                    WordCard(
                        id = it.id,
                        type = it.type.name,
                        expression = it.phrase,
                        translation = it.translation,
                        example_sentence = it.context,
                        level = it.level.name,
                        optionsRaw = it.options.joinToString("|"),
                        correctAnswer = it.correctAnswer,
                        category = it.category,
                        variationsRaw = it.variations.joinToString("||")
                    )
                }
                if (globalCards.isNotEmpty()) {
                    _uiState.value = currentState.copy(
                        learningCards = globalCards,
                        currentLearningCardIndex = 0,
                        showLearningCards = true,
                        isLearningCardRevealed = false
                    )
                    tts.speak(getSpeakText(globalCards[0], true))
                }
                return
            }
            _uiState.value = currentState.copy(
                learningCards = fallbackCards,
                currentLearningCardIndex = 0,
                showLearningCards = true,
                isLearningCardRevealed = false
            )
            tts.speak(getSpeakText(fallbackCards[0], true))
            return
        }
        
        _uiState.value = currentState.copy(
            learningCards = cards,
            currentLearningCardIndex = 0,
            showLearningCards = true,
            isLearningCardRevealed = false
        )
        tts.speak(getSpeakText(cards[0], true))
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
            // Açıklama/Örnek cümleyi de oku
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
                // Mock Gündem Feed URL'sini SharedPreferences'tan dinamik al
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

                    val jsonArray = JSONArray(response.toString())
                    val newCards = mutableListOf<WordCard>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        
                        val options = mutableListOf<String>()
                        val optsArray = obj.optJSONArray("options")
                        if (optsArray != null) {
                            for (j in 0 until optsArray.length()) {
                                options.add(optsArray.getString(j))
                            }
                        }

                        val variations = mutableListOf<String>()
                        val varsArray = obj.optJSONArray("variations")
                        if (varsArray != null) {
                            for (j in 0 until varsArray.length()) {
                                variations.add(varsArray.getString(j))
                            }
                        }

                        newCards.add(
                            WordCard(
                                id = obj.getInt("id"),
                                type = obj.getString("type"),
                                level = obj.getString("level"),
                                expression = obj.getString("phrase"),
                                translation = obj.getString("translation"),
                                example_sentence = obj.getString("context"),
                                correctAnswer = obj.getString("correctAnswer"),
                                optionsRaw = options.joinToString("|"),
                                category = obj.optString("category", "CASUAL"),
                                variationsRaw = variations.joinToString("||")
                            )
                        )
                    }

                    if (newCards.isNotEmpty()) {
                        repository.insertCards(newCards)
                        Log.d("MainScreenViewModel", "Foreground Sync Başarılı: ${newCards.size} yeni gündem sorusu eklendi.")
                    }
                } else {
                    throw Exception("HTTP error code: ${conn.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("MainScreenViewModel", "Foreground Sync çevrimdışı çalışıyor, yerel simülasyon çalıştırılıyor: ${e.message}")
                
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

    // Pratik modunda sıradaki soruya geçiş
    fun nextPracticeItem() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        loadNextPracticeItem(currentState.currentLevel)
    }

    // Metni seslendir (Gelen İngilizce metni ses sentezleyiciye gönderir)
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

    // İlerlemeyi sıfırlayıp onboarding'e döner
    fun resetProgress() {
        prefs.clearUserProgress()
        pendingCardUpdates.clear()
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
        
        _uiState.value = MainScreenUiState.OnboardingWelcome
    }

    override fun onCleared() {
        // View model sonlandığında bekleyen tüm kayıtları hemen diske yaz (Kayıp önleme)
        saveProgress()
        tts.shutdown()
        super.onCleared()
    }

    // --- Zaman Sayacı Metotları ---
    private fun startActiveTimer() {
        viewModelScope.launch {
            while (coroutineContext[kotlinx.coroutines.Job]?.isActive == true) {
                kotlinx.coroutines.delay(1000)
                secondsTimerActive += 1
                
                // Her 15 saniyede bir diske yaz ve sayacı sıfırla
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

    // Akıllı Bildirimleri Planlama (Günde 1 kez/12 saatte bir tetiklenecek şekilde)
    private fun scheduleNotifications(context: Context) {
        val notificationRequest = PeriodicWorkRequestBuilder<com.example.lingoscroll.sync.NotificationWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LingoScrollNotifications",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationRequest
        )
    }

    // Kullanıcının öğrenme tarzını değiştirir (İlerlemeyi sıfırlamaz)
    fun changeLearningStyle(newStyle: String) {
        prefs.setUserStyle(newStyle)
        // Eğer pratik modundaysak soruyu yeni tarza göre yeniden yükle
        val currentState = _uiState.value as? MainScreenUiState.Practice
        if (currentState != null) {
            loadNextPracticeItem(currentState.currentLevel)
        }
    }

    // Telaffuz okuma hızını değiştirir ve kaydeder
    fun changeTtsSpeechRate(rate: Float) {
        prefs.setTtsSpeechRate(rate)
        tts.setSpeechRate(rate)
    }

    // Özel Akış URL'sini günceller ve senkronizasyonu anında tetikler
    fun updateFeedUrl(newUrl: String) {
        prefs.setCustomFeedUrl(newUrl)
        triggerForegroundSync()
    }

    // Kayıtlı Özel Akış URL'sini döner
    fun getCustomFeedUrl(): String {
        return prefs.getCustomFeedUrl()
    }

    // Arayüz için anlık istatistik verilerini döner
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

// İstatistikleri temsil eden veri sınıfı
data class StudyStats(
    val streak: Int,
    val totalMinutes: Long,
    val totalAnswered: Int,
    val correctAnswered: Int,
    val accuracyPercent: Int
)
