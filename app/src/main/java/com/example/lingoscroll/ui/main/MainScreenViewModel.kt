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
import com.example.lingoscroll.data.repository.FirebaseLeaderboardRepository
import com.example.lingoscroll.data.repository.LeaderboardEntry
import com.example.lingoscroll.data.tts.NativeTtsManager
import com.example.lingoscroll.data.tts.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import com.example.lingoscroll.data.auth.AuthManager

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

    data class RedCodeSurvival(
        val currentItem: SurvivalCard,
        val timeLeftSeconds: Int,
        val totalScore: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val selectedOption: String? = null,
        val isAnswerEvaluated: Boolean = false,
        val isAnswerCorrect: Boolean = false,
        val showSummary: Boolean = false,
        
        // Skeleton mechanic fields
        val userInput: String = "",
        val jokerCount: Int = 0,
        val wrongLetter: String = "",
        val showErrorAnimation: Boolean = false,
        val revealedIndices: Set<Int> = emptySet(),
        val typedIndices: Set<Int> = emptySet(),
        
        val shuffledOptions: List<String> = emptyList(),
        val questionStartTime: Long = 0L
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
        val userInput: String = "",
        val jokerCount: Int = 0,
        val wrongLetter: String = "",
        val showErrorAnimation: Boolean = false,
        val revealedIndices: Set<Int> = emptySet(),
        val typedIndices: Set<Int> = emptySet(),
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

class MainScreenViewModel(private val context: Context) : ViewModel() {
    private val prefs = PreferencesManager(context)
    private val tts: TtsManager = NativeTtsManager(context)
    private val db = AppDatabase.getDatabase(context, viewModelScope)
    private val repository = CardRepository(db.cardDao())
    private val authManager = AuthManager()

    private val _uiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.OnboardingWelcome)
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val pendingCardUpdates = mutableMapOf<Int, Boolean>()
    private val failCountMap = mutableMapOf<Int, Int>()
    private val sessionQueue = mutableListOf<SurvivalCard>()
    private var secondsTimerActive = 0L

    // --- Aşama 3: Kırmızı Kod (Survival) Modu Alanları ---
    private var redCodeTimerJob: kotlinx.coroutines.Job? = null
    private val redCodeQueue = mutableListOf<SurvivalCard>()
    private var currentRedCodeIndex = 0

    // --- Aşama 4: Liderlik Tablosu Alanları ---
    private val leaderboardRepository = FirebaseLeaderboardRepository()
    private val _leaderboardState = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardState: StateFlow<List<LeaderboardEntry>> = _leaderboardState.asStateFlow()
    private val _isUploadingScore = MutableStateFlow(false)
    val isUploadingScore: StateFlow<Boolean> = _isUploadingScore.asStateFlow()
    private val _scoreUploadSuccess = MutableStateFlow<Boolean?>(null)
    val scoreUploadSuccess: StateFlow<Boolean?> = _scoreUploadSuccess.asStateFlow()

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

        // Eski feed.json senkronizasyon adresini yeni assets/survival_questions.json adresi ile değiştir
        val currentFeedUrl = prefs.getCustomFeedUrl()
        if (currentFeedUrl.contains("feed.json")) {
            prefs.setCustomFeedUrl("https://raw.githubusercontent.com/huseyinedepali77/LingoScroll/main/app/src/main/assets/survival_questions.json")
        }

        tts.setSpeechRate(prefs.getTtsSpeechRate())
        triggerForegroundSync()
        scheduleBackgroundSync(context)
        scheduleNotifications(context)
        startActiveTimer()

        // Firebase Anonim Kimlik Girişini tetikle
        authManager.signInAnonymously { user ->
            if (user != null) {
                Log.d("MainScreenViewModel", "Firebase Anonim Giriş Başarılı. Kullanıcı ID (UID): ${user.uid}")
            } else {
                Log.e("MainScreenViewModel", "Firebase Anonim Giriş Başarısız oldu.")
            }
        }

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
            val questions = repository.getStagePackage(category, exclude, System.currentTimeMillis(), prefs.getCurrentStage())
            
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
            userInput = "",
            jokerCount = 0,
            wrongLetter = "",
            showErrorAnimation = false,
            revealedIndices = calculateRevealedIndices(chosenItem.targetEn, chosenItem.difficulty),
            typedIndices = emptySet(),
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
            prefs.addXp(10) // Doğru cevap: +10 XP
        }

        _uiState.value = currentState.copy(
            selectedOption = answer,
            isAnswerEvaluated = true,
            isAnswerCorrect = isCorrect,
            secondsSaved = prefs.getTotalSecondsSaved()
        )

        tts.speak(currentState.currentItem.targetEn)
    }

    // Skeleton Giriş Alanı Değişimi (Single Key Interception ve Hayalet Klavye)
    fun onSkeletonInputChange(newInput: String) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return
        
        // Sadece son basılan karakteri yakala
        val typedChar = newInput.lastOrNull()
        if (typedChar == null) {
            _uiState.value = currentState.copy(userInput = "")
            return
        }
        
        val target = currentState.currentItem.targetEn
        
        // Henüz açılmamış ilk harfi bul (First Hidden Index)
        val firstHiddenIndex = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + currentState.typedIndices)
        }
        
        if (firstHiddenIndex != null) {
            val expectedChar = target[firstHiddenIndex]
            if (typedChar.lowercaseChar() == expectedChar.lowercaseChar()) {
                // Doğru harf basıldı, bu indeksi aç ve BasicTextField'ı sıfırla
                val newTypedIndices = currentState.typedIndices + firstHiddenIndex
                _uiState.value = currentState.copy(
                    userInput = "",
                    typedIndices = newTypedIndices
                )
                checkSkeletonFinished(newTypedIndices, target, currentState.jokerCount)
            } else {
                // Yanlış harf basıldı, hata animasyonunu bu ilk görünmeyen indekste tetikle
                _uiState.value = currentState.copy(
                    userInput = "",
                    wrongLetter = typedChar.toString(),
                    showErrorAnimation = true
                )
                viewModelScope.launch(Dispatchers.Main) {
                    delay(400L)
                    val latestState = _uiState.value as? MainScreenUiState.Practice ?: return@launch
                    _uiState.value = latestState.copy(
                        wrongLetter = "",
                        showErrorAnimation = false
                    )
                }
            }
        } else {
            _uiState.value = currentState.copy(userInput = "")
        }
    }

    // Joker (Harf Al) Butonuna Basıldığında
    fun useJoker() {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        if (currentState.isAnswerEvaluated) return
        
        val target = currentState.currentItem.targetEn
        val firstHiddenIndex = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + currentState.typedIndices)
        }
        
        if (firstHiddenIndex != null) {
            val newTypedIndices = currentState.typedIndices + firstHiddenIndex
            val newJokerCount = currentState.jokerCount + 1
            
            prefs.addXp(-5) // Joker kullanımı: -5 XP
            
            _uiState.value = currentState.copy(
                typedIndices = newTypedIndices,
                jokerCount = newJokerCount
            )
            
            checkSkeletonFinished(newTypedIndices, target, newJokerCount)
        }
    }

    // Otonom Tamamlanma Kontrolü ve Leitner Değerlendirmesi
    private fun checkSkeletonFinished(typedIndices: Set<Int>, target: String, jokerCount: Int) {
        val currentState = _uiState.value as? MainScreenUiState.Practice ?: return
        
        val nextHidden = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + typedIndices)
        }
        
        if (nextHidden == null) {
            // Tamamlandı! Leitner sistemi için joker sayısına göre değerlendir (3 veya daha fazla joker kullanıldıysa başarısız say)
            val isLeitnerCorrect = jokerCount < 3
            prefs.incrementQuestionsStats(isLeitnerCorrect)
            updateFailCount(currentState.currentItem.id, isLeitnerCorrect)
            
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateCardProgress(currentState.currentItem.id, isLeitnerCorrect)
            }
            
            if (isLeitnerCorrect) {
                val reward = if (jokerCount == 0) 25L else 10L
                prefs.addSecondsSaved(reward)
                prefs.addXp(10) // Doğru cevap: +10 XP
            }
            
            // Kullanıcı cümleyi eksiksiz tamamladığı için arayüzde HER ZAMAN başarılı/doğru göster
            _uiState.value = currentState.copy(
                userInput = "",
                jokerCount = jokerCount,
                isAnswerEvaluated = true,
                isAnswerCorrect = true,
                secondsSaved = prefs.getTotalSecondsSaved()
            )
            
            tts.speak(target)
        }
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
                        prefs.addXp(10) // Doğru cevap: +10 XP
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
            prefs.addXp(10) // Doğru cevap: +10 XP
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
            // Veritabanını sıfırladıktan sonra çevrimdışı soruları Room veritabanına yeniden yükle
            AppDatabase.loadOfflineQuestions(context, db.cardDao())
        }
        
        _uiState.value = MainScreenUiState.OnboardingWelcome
    }

    // --- Aşama 2: Titreşim ve Oyunlaştırma Getters/Setters ---
    fun isHapticEnabled(): Boolean {
        return prefs.isHapticEnabled()
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.setHapticEnabled(enabled)
    }

    fun getUserXp(): Int {
        return prefs.getUserXp()
    }

    fun getUserRank(): String {
        return prefs.getUserRank(prefs.getUserXp())
    }

    private fun calculateRevealedIndices(target: String, difficulty: Int): Set<Int> {
        val revealed = mutableSetOf<Int>()
        
        // 1. Boşluk ve noktalama işaretlerini her zaman açık tut
        for (i in target.indices) {
            val char = target[i]
            if (!char.isLetterOrDigit()) {
                revealed.add(i)
            }
        }
        
        // Kelime aralıklarını bul
        var inWord = false
        var wordStart = -1
        val wordRanges = mutableListOf<IntRange>()
        for (i in target.indices) {
            val isLetter = target[i].isLetter()
            if (isLetter && !inWord) {
                inWord = true
                wordStart = i
            } else if (!isLetter && inWord) {
                inWord = false
                wordRanges.add(wordStart until i)
            }
        }
        if (inWord) {
            wordRanges.add(wordStart until target.length)
        }
        
        when (difficulty) {
            1 -> {
                // Her kelimenin İLK ve SON harfini açık başlat
                for (range in wordRanges) {
                    if (range.first <= range.last) {
                        revealed.add(range.first)
                        revealed.add(range.last)
                    }
                }
            }
            2 -> {
                // Her kelimenin SADECE İLK harfini açık başlat
                for (range in wordRanges) {
                    if (range.first <= range.last) {
                        revealed.add(range.first)
                    }
                }
            }
            3 -> {
                // Sadece cümlenin en başındaki ilk harfi açık başlat
                if (wordRanges.isNotEmpty()) {
                    val firstWordRange = wordRanges.first()
                    revealed.add(firstWordRange.first)
                }
                // Uzunluğu 4 karakterden büyük olan kelimelerin sadece rastgele 1 harfini açık bırak
                for (i in wordRanges.indices) {
                    val range = wordRanges[i]
                    val wordLength = range.last - range.first + 1
                    if (i == 0) continue // İlk kelime zaten en baştaki kural ile açıldı
                    
                    if (wordLength > 4) {
                        val randomIdx = kotlin.random.Random.nextInt(range.first, range.last + 1)
                        revealed.add(randomIdx)
                    }
                }
            }
        }
        return revealed
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

    // --- Aşama 3: Kırmızı Kod (Survival) Modu Yönetim Fonksiyonları ---

    fun startRedCodeMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val questions = repository.getRedCodeCards().shuffled()
            if (questions.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.Main) {
                    redCodeQueue.clear()
                    redCodeQueue.addAll(questions)
                    currentRedCodeIndex = 0
                    
                    val firstItem = redCodeQueue[0]
                    val shuffledOpts = firstItem.optionsList.shuffled()
                    
                    _uiState.value = MainScreenUiState.RedCodeSurvival(
                        currentItem = firstItem,
                        timeLeftSeconds = 60,
                        totalScore = 0,
                        correctCount = 0,
                        wrongCount = 0,
                        selectedOption = null,
                        isAnswerEvaluated = false,
                        isAnswerCorrect = false,
                        showSummary = false,
                        
                        userInput = "",
                        jokerCount = 0,
                        wrongLetter = "",
                        showErrorAnimation = false,
                        revealedIndices = calculateRevealedIndices(firstItem.targetEn, firstItem.difficulty),
                        typedIndices = emptySet(),
                        
                        shuffledOptions = shuffledOpts,
                        questionStartTime = System.currentTimeMillis()
                    )
                    
                    startRedCodeTimer()
                }
            }
        }
    }

    private fun startRedCodeTimer() {
        redCodeTimerJob?.cancel()
        redCodeTimerJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000L)
                val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: break
                if (currentState.showSummary) break
                
                val nextTime = currentState.timeLeftSeconds - 1
                if (nextTime <= 0) {
                    val finalScore = currentState.totalScore
                    val finalCorrect = currentState.correctCount
                    _uiState.value = currentState.copy(
                        timeLeftSeconds = 0,
                        showSummary = true
                    )
                    
                    // Otomatik skor gönderme mantığı:
                    // Eğer 5 veya daha fazla doğru yapıldıysa ve önceden kaydedilmiş kullanıcı adı varsa arka planda otomatik gönder
                    if (finalCorrect >= 5) {
                        val savedNickname = prefs.getUserNickname()
                        if (savedNickname != null) {
                            submitLeaderboardScore(savedNickname, finalScore)
                        }
                    }
                    break
                } else {
                    _uiState.value = currentState.copy(timeLeftSeconds = nextTime)
                }
            }
        }
    }

    fun answerRedCodeQuestion(answer: String) {
        val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return
        if (currentState.isAnswerEvaluated) return
        
        val isCorrect = answer == currentState.currentItem.targetEn
        val elapsed = System.currentTimeMillis() - currentState.questionStartTime
        
        val pointsGained = if (isCorrect) {
            if (elapsed < 3000L) 150 else 100
        } else {
            -20
        }
        
        val newScore = (currentState.totalScore + pointsGained).coerceAtLeast(0)
        val newTimeLeft = if (isCorrect) currentState.timeLeftSeconds + 3 else currentState.timeLeftSeconds
        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount
        val newWrongCount = if (!isCorrect) currentState.wrongCount + 1 else currentState.wrongCount
        
        _uiState.value = currentState.copy(
            selectedOption = answer,
            isAnswerEvaluated = true,
            isAnswerCorrect = isCorrect,
            totalScore = newScore,
            timeLeftSeconds = newTimeLeft,
            correctCount = newCorrectCount,
            wrongCount = newWrongCount
        )
        
        tts.speak(currentState.currentItem.targetEn)
    }

    fun onRedCodeSkeletonInputChange(newInput: String) {
        val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return
        if (currentState.isAnswerEvaluated) return
        
        val target = currentState.currentItem.targetEn
        val firstHiddenIndex = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + currentState.typedIndices)
        } ?: return
        
        val expectedChar = target[firstHiddenIndex].lowercaseChar()
        val typedChar = newInput.lastOrNull()?.lowercaseChar()
        
        if (typedChar == expectedChar) {
            val newTyped = currentState.typedIndices + firstHiddenIndex
            _uiState.value = currentState.copy(
                userInput = "",
                typedIndices = newTyped,
                showErrorAnimation = false
            )
            
            checkRedCodeSkeletonFinished(newTyped, target, currentState.jokerCount)
        } else if (typedChar != null) {
            _uiState.value = currentState.copy(
                userInput = "",
                showErrorAnimation = true,
                wrongLetter = typedChar.toString()
            )
            viewModelScope.launch {
                delay(400L)
                val latestState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return@launch
                _uiState.value = latestState.copy(showErrorAnimation = false)
            }
        }
    }

    private fun checkRedCodeSkeletonFinished(typedIndices: Set<Int>, target: String, jokerCount: Int) {
        val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return
        
        val nextHidden = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + typedIndices)
        }
        
        if (nextHidden == null) {
            val elapsed = System.currentTimeMillis() - currentState.questionStartTime
            
            val pointsGained = if (jokerCount < 3) {
                if (elapsed < 3000L) 150 else 100
            } else {
                100
            }
            
            val newScore = (currentState.totalScore + pointsGained).coerceAtLeast(0)
            val newTimeLeft = currentState.timeLeftSeconds + 3
            val newCorrectCount = currentState.correctCount + 1
            
            _uiState.value = currentState.copy(
                userInput = "",
                isAnswerEvaluated = true,
                isAnswerCorrect = true,
                totalScore = newScore,
                timeLeftSeconds = newTimeLeft,
                correctCount = newCorrectCount
            )
            
            tts.speak(target)
        }
    }

    fun useRedCodeJoker() {
        val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return
        if (currentState.isAnswerEvaluated) return
        
        val target = currentState.currentItem.targetEn
        val firstHiddenIndex = target.indices.firstOrNull { i ->
            target[i].isLetterOrDigit() && i !in (currentState.revealedIndices + currentState.typedIndices)
        }
        
        if (firstHiddenIndex != null) {
            val newTypedIndices = currentState.typedIndices + firstHiddenIndex
            val newJokerCount = currentState.jokerCount + 1
            val newScore = (currentState.totalScore - 50).coerceAtLeast(0)
            
            _uiState.value = currentState.copy(
                typedIndices = newTypedIndices,
                jokerCount = newJokerCount,
                totalScore = newScore
            )
            
            checkRedCodeSkeletonFinished(newTypedIndices, target, newJokerCount)
        }
    }

    fun nextRedCodeQuestion() {
        val currentState = _uiState.value as? MainScreenUiState.RedCodeSurvival ?: return
        
        currentRedCodeIndex++
        if (currentRedCodeIndex >= redCodeQueue.size) {
            redCodeQueue.shuffle()
            currentRedCodeIndex = 0
        }
        
        if (redCodeQueue.isNotEmpty()) {
            val nextItem = redCodeQueue[currentRedCodeIndex]
            val shuffledOpts = nextItem.optionsList.shuffled()
            
            _uiState.value = currentState.copy(
                currentItem = nextItem,
                selectedOption = null,
                isAnswerEvaluated = false,
                isAnswerCorrect = false,
                userInput = "",
                jokerCount = 0,
                wrongLetter = "",
                showErrorAnimation = false,
                revealedIndices = calculateRevealedIndices(nextItem.targetEn, nextItem.difficulty),
                typedIndices = emptySet(),
                shuffledOptions = shuffledOpts,
                questionStartTime = System.currentTimeMillis()
            )
        }
    }

    fun exitRedCodeMode() {
        redCodeTimerJob?.cancel()
        val category = prefs.getUserStyle()
        startStageSession(category)
    }

    fun retryRedCodeMode() {
        startRedCodeMode()
    }

    // --- Aşama 4: Liderlik Tablosu Fonksiyonları ---

    fun getCurrentUserUid(): String {
        return authManager.getCurrentUser()?.uid ?: "anonymous_user"
    }

    fun submitLeaderboardScore(name: String, score: Int) {
        val uid = getCurrentUserUid()
        val rank = prefs.getUserRank(prefs.getUserXp())
        
        _isUploadingScore.value = true
        _scoreUploadSuccess.value = null
        
        // Kalıcı ismi ayarla (Eğer ilk kez girildiyse kaydet)
        prefs.setUserNickname(name)
        
        viewModelScope.launch(Dispatchers.IO) {
            val entry = LeaderboardEntry(
                uid = uid,
                name = name,
                score = score,
                rank = rank,
                timestamp = System.currentTimeMillis()
            )
            val success = leaderboardRepository.submitScore(entry)
            viewModelScope.launch(Dispatchers.Main) {
                _isUploadingScore.value = false
                _scoreUploadSuccess.value = success
                if (success) {
                    fetchLeaderboard()
                }
            }
        }
    }

    fun fetchLeaderboard() {
        viewModelScope.launch(Dispatchers.IO) {
            val topScores = leaderboardRepository.getTopScores(50)
            viewModelScope.launch(Dispatchers.Main) {
                _leaderboardState.value = topScores
            }
        }
    }

    fun redoCurrentStage() {
        prefs.setStageProgress(0)
        val category = prefs.getUserStyle()
        startStageSession(category)
    }

    fun getUserNickname(): String? {
        return prefs.getUserNickname()
    }
}

data class StudyStats(
    val streak: Int,
    val totalMinutes: Long,
    val totalAnswered: Int,
    val correctAnswered: Int,
    val accuracyPercent: Int
)
