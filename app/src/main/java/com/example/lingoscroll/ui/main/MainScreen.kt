package com.example.lingoscroll.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.lingoscroll.data.local.SurvivalCard
import com.example.lingoscroll.theme.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlin.OptIn
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import com.example.lingoscroll.data.repository.LeaderboardEntry


@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val s = state) {
            is MainScreenUiState.OnboardingWelcome -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WelcomeScreen(onStart = { viewModel.startStressTest() })
                }
            }
            is MainScreenUiState.OnboardingQuiz -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    StressTestQuizScreen(
                        state = s,
                        isHapticEnabled = viewModel.isHapticEnabled(),
                        onAnswer = { viewModel.answerStressTestQuestion(it) },
                        onNext = { viewModel.nextStressTestQuestion() }
                    )
                }
            }
            is MainScreenUiState.OnboardingLevelReveal -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StressTestRevealScreen(
                        state = s,
                        onStartPractice = { viewModel.startLearningAfterStressTest() }
                    )
                }
            }
            is MainScreenUiState.Practice -> {
                PracticeScreen(
                    state = s,
                    viewModel = viewModel
                )
            }
            is MainScreenUiState.RedCodeSurvival -> {
                RedCodeSurvivalScreen(
                    state = s,
                    viewModel = viewModel
                )
            }
        }
    }
}

// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LingoScroll 🚨",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Survival Edition (V2)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = SurvivalDanger,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Yurt dışına çıktığınızda karşılaşabileceğiniz tüm kriz, ulaşım, hesap ödeme ve temel ihtiyaç sorunlarına yönelik saha simülasyonu.",
                fontSize = 15.sp,
                color = SurvivalText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = SurvivalPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Saha Stres Testini Başlat 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// 2. STRESS TEST QUIZ SCREEN
@Composable
fun StressTestQuizScreen(
    state: MainScreenUiState.OnboardingQuiz,
    isHapticEnabled: Boolean,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Saha Stres Testi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalText
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.currentQuestionIndex + 1).toFloat() / state.totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SurvivalDanger,
                trackColor = SurvivalBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${state.currentQuestionIndex + 1} / ${state.totalQuestions}",
                fontSize = 14.sp,
                color = SurvivalTextSecondary
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cleanScenarioText(state.currentQuestion.scenarioTr),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = SurvivalText,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.shuffledOptions.forEach { option ->
                val isSelected = state.selectedOption == option
                val isCorrect = option == state.currentQuestion.targetEn
                val btnColor = when {
                    state.selectedOption == null -> SurvivalSurface
                    isSelected && isCorrect -> SurvivalPrimary
                    isSelected && !isCorrect -> SurvivalDanger
                    !isSelected && isCorrect -> SurvivalPrimary
                    else -> SurvivalSurface
                }
                val txtColor = if (btnColor == SurvivalSurface) SurvivalText else Color.White

                Button(
                    onClick = {
                        if (state.selectedOption == null) {
                            onAnswer(option)
                            if (isHapticEnabled) {
                                if (isCorrect) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, SurvivalBorder, RoundedCornerShape(14.dp)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = txtColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.selectedOption != null) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = SurvivalText),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Devam Et ➡️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 3. STRESS TEST REVEAL SCREEN
@Composable
fun StressTestRevealScreen(
    state: MainScreenUiState.OnboardingLevelReveal,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Stres Testi Tamamlandı! 🎯",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Skorunuz: ${state.correctCount} / 5",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SurvivalText
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Belirlenen Başlangıç Odağı:",
                fontSize = 14.sp,
                color = SurvivalTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.categoryFocus,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalDanger
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Başlangıç Aşaması: Aşama ${state.startingStage}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SurvivalText
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartPractice,
                colors = ButtonDefaults.buttonColors(containerColor = SurvivalPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Eğitime Başla 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// 4. PRACTICE SCREEN
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    state: MainScreenUiState.Practice,
    viewModel: MainScreenViewModel
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            try {
                scrollState.animateScrollTo(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Cevap değerlendirildiğinde (klavye kapanıp ekran boyutu genişlediğinde) odağı temizle ve 300ms gecikmeyle üste kaydır
    LaunchedEffect(state.isAnswerEvaluated) {
        if (state.isAnswerEvaluated) {
            focusManager.clearFocus() // Odağı kaldırarak scroll kitlenmesini engelle
            keyboardController?.hide() // Klavyeyi gizle
            delay(300L) // Klavyenin kapanıp ekranın eski boyutuna gelmesini bekle
            try {
                scrollState.animateScrollTo(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Yeni kart yüklendiğinde odağı temizle ve ekranı anında en üste sıfırla
    LaunchedEffect(state.currentItem.id) {
        focusManager.clearFocus()
        try {
            scrollState.scrollTo(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        bottomBar = {
            if (!isImeVisible) {
                SurvivalBottomNavigation(
                    activeCategory = state.currentCategory,
                    onCategoryChange = { viewModel.changeCategory(it) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .background(SurvivalBg)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Progress Bar, Streak, Seconds Saved)
                PracticeHeader(
                    state = state,
                    xp = viewModel.getUserXp(),
                    rank = viewModel.getUserRank(),
                    onSettingsClick = { showSettingsDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))



                // Active Scenario Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Senaryo (${state.currentItem.category})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurvivalDanger,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cleanScenarioText(state.currentItem.scenarioTr),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SurvivalText,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Mechanics Block
                InteractiveMechanicCard(
                    state = state,
                    viewModel = viewModel,
                    isHapticEnabled = viewModel.isHapticEnabled(),
                    haptic = haptic
                )

                // Dialogs
                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = { Text("Operasyon Merkezi (Ayarlar)", fontWeight = FontWeight.Bold, color = SurvivalText) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cihaz Titreşimi (Haptic)", color = SurvivalText, fontSize = 15.sp)
                                    Switch(
                                        checked = viewModel.isHapticEnabled(),
                                        onCheckedChange = { viewModel.setHapticEnabled(it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SurvivalPrimary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchLeaderboard()
                                        showLeaderboardDialog = true
                                        showSettingsDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurvivalPrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🏆 Liderlik Tablosu", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showResetConfirmation = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurvivalDanger),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Verileri Sıfırla", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Kapat", color = SurvivalPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = SurvivalSurface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (showResetConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showResetConfirmation = false },
                        title = { Text("Emin misiniz?", fontWeight = FontWeight.Bold, color = SurvivalDanger) },
                        text = {
                            Text(
                                "Bu işlem tüm rütbe, XP ve Leitner ilerleme geçmişinizi kalıcı olarak silecek. Bu işlem geri alınamaz!",
                                color = SurvivalText
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.resetProgress()
                                    showResetConfirmation = false
                                    showSettingsDialog = false
                                }
                            ) {
                                Text("Evet, Sıfırla", color = SurvivalDanger, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetConfirmation = false }) {
                                Text("İptal", color = SurvivalTextSecondary)
                            }
                        },
                        containerColor = SurvivalSurface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (showLeaderboardDialog) {
                    val leaderboardEntries by viewModel.leaderboardState.collectAsState()
                    val myUid = viewModel.getCurrentUserUid()

                    AlertDialog(
                        onDismissRequest = { showLeaderboardDialog = false },
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Küresel Liderlik Tablosu", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                IconButton(
                                    onClick = { viewModel.fetchLeaderboard() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("🔄", fontSize = 16.sp)
                                }
                            }
                        },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                            ) {
                                if (leaderboardEntries.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Yükleniyor veya henüz skor yok...", color = Color.LightGray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        itemsIndexed(leaderboardEntries) { index, entry ->
                                            val isMe = entry.uid == myUid
                                            val borderStroke = if (isMe) {
                                                androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFBC02D))
                                            } else {
                                                androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF332222))
                                            }
                                            val medal = when (index) {
                                                0 -> "🥇 "
                                                1 -> "🥈 "
                                                2 -> "🥉 "
                                                else -> "${index + 1}. "
                                            }

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isMe) Color(0xFF2B2211) else Color(0xFF1E1414)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                border = borderStroke
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = medal,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp,
                                                            color = if (isMe) Color(0xFFFBC02D) else Color.White
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Column {
                                                            val shortId = if (entry.uid.length >= 4) entry.uid.takeLast(4).uppercase() else "0000"
                                                            Text(
                                                                text = buildAnnotatedString {
                                                                    append(entry.name)
                                                                    withStyle(SpanStyle(color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Normal)) {
                                                                        append(" #$shortId")
                                                                    }
                                                                },
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = entry.rank,
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = "${entry.score} Pts",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = if (isMe) Color(0xFFFBC02D) else Color(0xFF81C784)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showLeaderboardDialog = false }) {
                                Text("Kapat", color = SurvivalPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = Color(0xFF160E0E),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Alt kısımdaki kayan değerlendirme kartı için boşluk bırak
                Spacer(modifier = Modifier.height(120.dp))
            }

            // Alt Kısımda Yüzen Değerlendirme Kartı (Evaluation Block)
            if (state.isAnswerEvaluated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    EvaluationResultBanner(
                        isCorrect = state.isAnswerCorrect,
                        correctAnswer = state.currentItem.targetEn,
                        onNext = { viewModel.nextPracticeQuestion() }
                    )
                }
            }
        }

        // Stage Complete Dialog
        if (state.showStageComplete) {
            StageCompleteOverlay(
                stage = state.currentStage,
                onProceed = { viewModel.startRedCodeMode() }
            )
        }
    }
}

// SUB-COMPONENTS FOR PRACTICE SCREEN

@Composable
fun PracticeHeader(
    state: MainScreenUiState.Practice,
    xp: Int,
    rank: String,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rütbe ve XP Rozetleri Satırı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Rütbe Rozeti (Ajan Rozeti)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(SurvivalPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("🎖️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rank,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurvivalPrimary
                )
            }

            // Sağ Taraf: XP Miktarı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(SurvivalDanger.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("⭐", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$xp XP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurvivalDanger
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aşama ${state.currentStage}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔥 ${state.streak}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurvivalDanger
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "⏱️ ${state.secondsSaved}s",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurvivalPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Ayarlar Çark Butonu
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("⚙️", fontSize = 18.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { state.stageProgress.toFloat() / 15f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SurvivalPrimary,
            trackColor = SurvivalBorder
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${state.stageProgress} / 15 Soru",
            fontSize = 12.sp,
            color = SurvivalTextSecondary
        )
    }
}

@Composable
fun InteractiveMechanicCard(
    state: MainScreenUiState.Practice,
    viewModel: MainScreenViewModel,
    isHapticEnabled: Boolean,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state.currentItem.mechanicType) {
                "SKELETON" -> {
                    SkeletonMechanicView(
                        state = state,
                        isHapticEnabled = isHapticEnabled,
                        onInputChange = { viewModel.onSkeletonInputChange(it) },
                        onUseJoker = { viewModel.useJoker() }
                    )
                }
                "SWIPE" -> {
                    SwipeMechanicView(state = state, onAnswer = { viewModel.answerPracticeQuestion(it) })
                }
                "CHUNK" -> {
                    ChunkMechanicView(
                        state = state,
                        onClickChunk = { viewModel.clickChunk(it) },
                        onClearChunks = { viewModel.clearClickedChunks() }
                    )
                }
                "ERROR_FIND" -> {
                    ErrorFindMechanicView(state = state, onClickWord = { viewModel.clickErrorWord(it) })
                }
                else -> {
                    // Fallback to Skeleton
                    SkeletonMechanicView(
                        state = state,
                        isHapticEnabled = isHapticEnabled,
                        onInputChange = { viewModel.onSkeletonInputChange(it) },
                        onUseJoker = { viewModel.useJoker() }
                    )
                }
            }
        }
    }
}

// 4.1 SKELETON VIEW
@Composable
fun SkeletonMechanicView(
    state: MainScreenUiState.Practice,
    isHapticEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onUseJoker: () -> Unit
) {
    val firstHiddenIndex = remember(state.currentItem.targetEn, state.revealedIndices, state.typedIndices) {
        state.currentItem.targetEn.indices.firstOrNull { i ->
            state.currentItem.targetEn[i].isLetterOrDigit() && i !in (state.revealedIndices + state.typedIndices)
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Klavye giriş hafızasını sıfırlayan ve peş peşe aynı harflerin yutulmasını önleyen yerel durum
    var textState by remember(state.currentItem.id) {
        mutableStateOf(TextFieldValue(""))
    }
    
    // Soru değiştiğinde klavyeyi otomatik odakla (Gecikmeli ve try-catch korumalı)
    LaunchedEffect(state.currentItem.id) {
        delay(100L)
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(state.showErrorAnimation) {
        if (state.showErrorAnimation && isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val errorAlpha by animateFloatAsState(
        targetValue = if (state.showErrorAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "ErrorAlpha"
    )

    val annotatedSkeleton = remember(state.currentItem.targetEn, state.userInput, state.revealedIndices, state.typedIndices, state.showErrorAnimation, state.wrongLetter, errorAlpha, firstHiddenIndex) {
        buildAnnotatedString {
            val target = state.currentItem.targetEn
            val activeRevealed = state.revealedIndices + state.typedIndices
            for (i in target.indices) {
                val char = target[i]
                if (i < state.userInput.length || i in activeRevealed) {
                    append(char)
                } else if (i == firstHiddenIndex && state.showErrorAnimation) {
                    // Yanlış harfi kırmızı renkte ve azalan alpha ile tam yerinde (in-place) çiz!
                    withStyle(SpanStyle(color = SurvivalDanger.copy(alpha = errorAlpha), fontWeight = FontWeight.Bold)) {
                        append(state.wrongLetter.firstOrNull() ?: '_')
                    }
                } else {
                    if (char.isLetterOrDigit()) {
                        append('_')
                    } else {
                        append(char)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                try {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } // Kartın boş yerine tıklandığında da klavyeyi aç
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "İskelet Cümle (Yazmaya Başlayın):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SurvivalDanger
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clickable {
                    try {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = annotatedSkeleton,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SurvivalText,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onUseJoker,
                enabled = !state.isAnswerEvaluated,
                colors = ButtonDefaults.buttonColors(containerColor = SurvivalDanger),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💡 Harf Al", color = Color.White)
            }
            
            Text(
                text = "Joker Sayacı: ${state.jokerCount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (state.jokerCount >= 3) SurvivalDanger else SurvivalTextSecondary
            )
        }

        // Görünmez Klavye Giriş Alanı (Hayalet Klavye) - Odaklandığında sayfanın en altına kaydırılması için alta konumlandırıldı
        BasicTextField(
            value = textState,
            onValueChange = { newValue ->
                textState = newValue
                val char = newValue.text.lastOrNull()
                if (char != null) {
                    onInputChange(newValue.text)
                    // Klavyenin dahili composing/hafıza tamponunu anında sıfırla (aynı harfin yutulmasını kesinlikle engeller!)
                    textState = TextFieldValue(text = "", selection = TextRange.Zero, composition = null)
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f)
        )
    }
}

// 4.2 SWIPE VIEW
@Composable
fun SwipeMechanicView(
    state: MainScreenUiState.Practice,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.shuffledOptions.forEach { option ->
            val isSelected = state.selectedOption == option
            val isCorrect = option == state.currentItem.targetEn
            val btnColor = when {
                state.selectedOption == null -> SurvivalSurface
                isSelected && isCorrect -> SurvivalPrimary
                isSelected && !isCorrect -> SurvivalDanger
                !isSelected && isCorrect -> SurvivalPrimary
                else -> SurvivalSurface
            }
            val txtColor = if (btnColor == SurvivalSurface) SurvivalText else Color.White

            Button(
                onClick = { if (state.selectedOption == null) onAnswer(option) },
                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, SurvivalBorder, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = option,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = txtColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 4.3 CHUNK VIEW
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChunkMechanicView(
    state: MainScreenUiState.Practice,
    onClickChunk: (String) -> Unit,
    onClearChunks: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Blokları Doğru Sırayla Birleştir:",
            fontSize = 13.sp,
            color = SurvivalTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Birleştirilen Kelimeler (Tekil Metin Alanı / Unified Sentence Box)
        val joinedSentence = remember(state.clickedChunks) {
            state.clickedChunks.joinToString(" ")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .background(SurvivalSurface, RoundedCornerShape(12.dp))
                .border(1.dp, SurvivalBorder, RoundedCornerShape(12.dp))
                .clickable(enabled = !state.isAnswerEvaluated) { onClearChunks() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (joinedSentence.isEmpty()) "Parçaları birleştirmek için aşağıya dokunun..." else joinedSentence,
                color = if (joinedSentence.isEmpty()) SurvivalTextSecondary else SurvivalText,
                fontWeight = FontWeight.Bold,
                fontSize = when {
                    joinedSentence.length > 40 -> 13.sp
                    joinedSentence.length > 25 -> 15.sp
                    else -> 18.sp
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Karıştırılmış Buton Havuzu (FlowRow ile taşmalar engellendi)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.shuffledChunks.forEach { chunk ->
                val isSelected = chunk in state.clickedChunks
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(
                            if (isSelected) SurvivalBorder else SurvivalSurface,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, SurvivalBorder, RoundedCornerShape(8.dp))
                        .clickable(enabled = !isSelected && !state.isAnswerEvaluated) { onClickChunk(chunk) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .wrapContentWidth()
                ) {
                    Text(
                        text = chunk,
                        color = if (isSelected) Color.Gray else SurvivalText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// 4.4 ERROR FIND VIEW
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ErrorFindMechanicView(
    state: MainScreenUiState.Practice,
    onClickWord: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dolandırıcı Radarı: Hatalı kelimeye dokun!",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SurvivalDanger,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val words = remember(state.errorSentenceText) {
            state.errorSentenceText.split(" ")
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEach { word ->
                val isClicked = state.tappedErrorWord == word
                val bgColor = when {
                    !isClicked -> SurvivalSurface
                    state.isErrorCorrected -> SurvivalPrimary
                    else -> SurvivalDanger
                }
                val txtColor = if (bgColor == SurvivalSurface) SurvivalText else Color.White

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, SurvivalBorder, RoundedCornerShape(8.dp))
                        .clickable(enabled = !state.isAnswerEvaluated) { onClickWord(word) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .wrapContentWidth()
                ) {
                    Text(
                        text = word,
                        color = txtColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// 5. EVALUATION RESULT BANNER
@Composable
fun EvaluationResultBanner(
    isCorrect: Boolean,
    correctAnswer: String,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) SurvivalPrimary else SurvivalDanger
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCorrect) "Tebrikler! Doğru 🥳" else "Hatalı Cevap 🥺",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Doğru Cevap: $correctAnswer",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Sıradaki Kalıp ➡️",
                    color = if (isCorrect) SurvivalPrimary else SurvivalDanger,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 6. STAGE COMPLETE OVERLAY
@Composable
fun StageCompleteOverlay(
    stage: Int,
    onProceed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1414)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏆 Aşama $stage Tamamlandı! 🏆",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFB300),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Yeni aşamanın kilidini açmak için 60 saniyelik Hayatta Kalma Testi'ni (Quiz) geçmeniz gerekiyor. En az 5 doğru yapmalısınız!",
                    fontSize = 15.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Testi Başlat ⚡",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// 7. SURVIVAL BOTTOM NAVIGATION PANEL
@Composable
fun SurvivalBottomNavigation(
    activeCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val items = listOf(
        Triple("CRISIS", "🚨", "Kriz"),
        Triple("NAVIGATION", "🧭", "Yol"),
        Triple("FINANCE", "💳", "Para"),
        Triple("BASIC_NEEDS", "🛍️", "İhtiyaç"),
        Triple("MIXED", "🔄", "Karışık")
    )

    NavigationBar(
        containerColor = SurvivalSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        items.forEach { (cat, emoji, name) ->
            val isActive = activeCategory == cat
            NavigationBarItem(
                selected = isActive,
                onClick = { onCategoryChange(cat) },
                icon = {
                    Text(
                        text = emoji,
                        fontSize = 24.sp
                    )
                },
                label = {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) SurvivalPrimary else SurvivalTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SurvivalPrimary,
                    unselectedIconColor = SurvivalTextSecondary,
                    indicatorColor = SurvivalPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// HELPERS
fun toSkeletonText(sentence: String): String {
    return sentence.split(" ").map { word ->
        if (word.isEmpty()) return@map ""
        
        val firstLetterIdx = word.indexOfFirst { it.isLetter() }
        if (firstLetterIdx == -1) {
            return@map word
        }
        
        val sb = StringBuilder()
        for (i in word.indices) {
            val c = word[i]
            if (i < firstLetterIdx) {
                sb.append(c)
            } else if (i == firstLetterIdx) {
                sb.append(c)
            } else {
                if (c.isLetter()) {
                    sb.append('_')
                } else {
                    sb.append(c)
                }
            }
        }
        sb.toString()
    }.joinToString(" ")
}

fun toDynamicSkeletonText(target: String, userInput: String, revealedIndices: Set<Int>): String {
    val sb = java.lang.StringBuilder()
    for (i in target.indices) {
        val char = target[i]
        if (i < userInput.length || i in revealedIndices) {
            sb.append(char)
        } else {
            if (char.isLetterOrDigit()) {
                sb.append('_')
            } else {
                sb.append(char)
            }
        }
    }
    return sb.toString()
}

fun cleanScenarioText(text: String): String {
    return text.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
}

// 8. KIRMIZI KOD (SURVIVAL) MODE SCREEN
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RedCodeSurvivalScreen(
    state: MainScreenUiState.RedCodeSurvival,
    viewModel: MainScreenViewModel
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    var showNameDialog by remember { mutableStateOf(false) }
    var codeName by remember { mutableStateOf("") }
    var showLeaderboardDialog by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            try {
                scrollState.animateScrollTo(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(state.isAnswerEvaluated) {
        if (state.isAnswerEvaluated) {
            focusManager.clearFocus()
            keyboardController?.hide()
            delay(300L)
            try {
                scrollState.animateScrollTo(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(state.currentItem.id) {
        focusManager.clearFocus()
        try {
            scrollState.scrollTo(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(state.showSummary) {
        if (state.showSummary) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .background(Color(0xFF0F0B0B)) // Velvet Mat Kırmızımsı Koyu Arka Plan
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Survival Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isUrgent = state.timeLeftSeconds <= 15
                val timerBg = if (isUrgent) Color(0xFF451313) else Color(0xFF221414)
                val timerColor = if (isUrgent) Color(0xFFFE2525) else Color(0xFFFFB300)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(timerBg, RoundedCornerShape(12.dp))
                        .border(1.dp, timerColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(if (isUrgent) "🚨" else "⏱️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.timeLeftSeconds}s",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = timerColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF132213), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF81C784).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.totalScore} Pts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF81C784)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Senaryo Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E1F1F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACİL DURUM SENARYOSU",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cleanScenarioText(state.currentItem.scenarioTr),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mekanik Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E1F1F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (state.currentItem.mechanicType == "SKELETON") {
                        SkeletonMechanicView(
                            state = MainScreenUiState.Practice(
                                currentItem = state.currentItem,
                                streak = 0,
                                secondsSaved = 0,
                                currentCategory = "MIXED",
                                userInput = state.userInput,
                                jokerCount = state.jokerCount,
                                wrongLetter = state.wrongLetter,
                                showErrorAnimation = state.showErrorAnimation,
                                revealedIndices = state.revealedIndices,
                                typedIndices = state.typedIndices
                            ),
                            isHapticEnabled = viewModel.isHapticEnabled(),
                            onInputChange = { viewModel.onRedCodeSkeletonInputChange(it) },
                            onUseJoker = { viewModel.useRedCodeJoker() }
                        )
                    } else {
                        SwipeMechanicView(
                            state = MainScreenUiState.Practice(
                                currentItem = state.currentItem,
                                streak = 0,
                                secondsSaved = 0,
                                currentCategory = "MIXED",
                                shuffledOptions = state.shuffledOptions,
                                selectedOption = state.selectedOption
                            ),
                            onAnswer = { viewModel.answerRedCodeQuestion(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        if (state.isAnswerEvaluated) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                EvaluationResultBanner(
                    isCorrect = state.isAnswerCorrect,
                    correctAnswer = state.currentItem.targetEn,
                    onNext = { viewModel.nextRedCodeQuestion() }
                )
            }
        }

        // Özet Ekranı
        if (state.showSummary) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1414)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isPassed = state.correctCount >= 5
                        Text(
                            text = if (isPassed) "🎉 TEST BAŞARILI! 🎉" else "❌ TEST BAŞARISIZ! ❌",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPassed) Color(0xFF81C784) else Color(0xFFFF5252),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPassed) "Yeni aşamanın kilidi açıldı!" else "Yeterli doğru cevaba ulaşamadınız. Bu aşamayı tekrar etmelisiniz (En az 5 doğru gerekli).",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Toplam Skor:",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "${state.totalScore} Puan",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF81C784)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doğru", color = Color.Gray, fontSize = 12.sp)
                                Text("${state.correctCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yanlış", color = Color.Gray, fontSize = 12.sp)
                                Text("${state.wrongCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        if (isPassed) {
                            // Skor Gönder & Liderlik Tablosu Bölümü (Sadece başarılı testlerde)
                            val savedNickname = viewModel.getUserNickname()
                            if (savedNickname != null) {
                                val uploadSuccess by viewModel.scoreUploadSuccess.collectAsState()
                                val isUploading by viewModel.isUploadingScore.collectAsState()
                                when {
                                    uploadSuccess == true -> {
                                        Text("Skor Otomatik Yüklendi! ✅\n(Kod Adı: $savedNickname)", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                    }
                                    isUploading -> {
                                        Text("Skor Gönderiliyor... ⏳", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    else -> {
                                        Text("Skor Otomatik Yükleniyor... ⏳", color = Color.LightGray, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                if (state.totalScore > 0) {
                                    val uploadSuccess by viewModel.scoreUploadSuccess.collectAsState()
                                    val isUploading by viewModel.isUploadingScore.collectAsState()
                                    when {
                                        uploadSuccess == true -> {
                                            Text("Skor Başarıyla Gönderildi! ✅", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        isUploading -> {
                                            Text("Skor Gönderiliyor... ⏳", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        else -> {
                                            Button(
                                                onClick = { showNameDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                            ) {
                                                Text("Skoru Gönder 🏆", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.fetchLeaderboard()
                                    showLeaderboardDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332222)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Text("Liderlik Tablosu 🏅", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { viewModel.proceedToNextStage() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Yeni Aşamaya Geç ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Başarısız Test: Aşamayı Tekrar Etme Butonu
                            Button(
                                onClick = { viewModel.redoCurrentStage() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Aşamayı Tekrar Et 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Dialogs for name entry and leaderboard display inside RedCodeSurvivalScreen
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Kod Adınızı Girin", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = codeName,
                        onValueChange = { codeName = it },
                        placeholder = { Text("Örn: Ajan_007", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SurvivalPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (codeName.isNotBlank()) {
                                viewModel.submitLeaderboardScore(codeName, state.totalScore)
                                showNameDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurvivalPrimary)
                    ) {
                        Text("Gönder", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("İptal", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF1D1414),
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showLeaderboardDialog) {
            val leaderboardEntries by viewModel.leaderboardState.collectAsState()
            val myUid = viewModel.getCurrentUserUid()

            AlertDialog(
                onDismissRequest = { showLeaderboardDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Küresel Liderlik Tablosu", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        IconButton(
                            onClick = { viewModel.fetchLeaderboard() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("🔄", fontSize = 16.sp)
                        }
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        if (leaderboardEntries.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Yükleniyor veya henüz skor yok...", color = Color.LightGray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(leaderboardEntries) { index, entry ->
                                    val isMe = entry.uid == myUid
                                    val borderStroke = if (isMe) {
                                        androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFBC02D))
                                    } else {
                                        androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF332222))
                                    }
                                    val medal = when (index) {
                                        0 -> "🥇 "
                                        1 -> "🥈 "
                                        2 -> "🥉 "
                                        else -> "${index + 1}. "
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMe) Color(0xFF2B2211) else Color(0xFF1E1414)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = borderStroke
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = medal,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = if (isMe) Color(0xFFFBC02D) else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column {
                                                    val shortId = if (entry.uid.length >= 4) entry.uid.takeLast(4).uppercase() else "0000"
                                                    Text(
                                                        text = buildAnnotatedString {
                                                            append(entry.name)
                                                            withStyle(SpanStyle(color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Normal)) {
                                                                append(" #$shortId")
                                                            }
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = entry.rank,
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${entry.score} Pts",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = if (isMe) Color(0xFFFBC02D) else Color(0xFF81C784)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLeaderboardDialog = false }) {
                        Text("Kapat", color = SurvivalPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF160E0E),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

