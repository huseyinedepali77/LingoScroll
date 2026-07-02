package com.example.lingoscroll.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items


@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                            if (isCorrect) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
@Composable
fun PracticeScreen(
    state: MainScreenUiState.Practice,
    viewModel: MainScreenViewModel
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        bottomBar = {
            SurvivalBottomNavigation(
                activeCategory = state.currentCategory,
                onCategoryChange = { viewModel.changeCategory(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurvivalBg)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Progress Bar, Streak, Seconds Saved)
                PracticeHeader(state = state)

                // Active Scenario Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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

                        // Mechanics Block
                        InteractiveMechanicCard(
                            state = state,
                            viewModel = viewModel,
                            haptic = haptic
                        )
                    }
                }

                // Evaluation Block (Bottom overlay when answered)
                if (state.isAnswerEvaluated) {
                    EvaluationResultBanner(
                        isCorrect = state.isAnswerCorrect,
                        correctAnswer = state.currentItem.targetEn,
                        onNext = { viewModel.nextPracticeQuestion() }
                    )
                }
            }

            // Stage Complete Dialog
            if (state.showStageComplete) {
                StageCompleteOverlay(
                    stage = state.currentStage,
                    onProceed = { viewModel.proceedToNextStage() }
                )
            }
        }
    }
}

// SUB-COMPONENTS FOR PRACTICE SCREEN

@Composable
fun PracticeHeader(state: MainScreenUiState.Practice) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
    onInputChange: (String) -> Unit,
    onUseJoker: () -> Unit
) {
    val dynamicSkeleton = remember(state.currentItem.targetEn, state.userInput) {
        toDynamicSkeletonText(state.currentItem.targetEn, state.userInput)
    }

    val focusRequester = remember { FocusRequester() }
    
    // Soru değiştiğinde klavyeyi otomatik odakla
    LaunchedEffect(state.currentItem.id) {
        focusRequester.requestFocus()
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(state.showErrorAnimation) {
        if (state.showErrorAnimation) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val errorAlpha by animateFloatAsState(
        targetValue = if (state.showErrorAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "ErrorAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() } // Tıklandığında klavyeyi aç/odakla
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Görünmez Klavye Giriş Alanı (Hayalet Klavye)
        BasicTextField(
            value = state.userInput,
            onValueChange = onInputChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(0.dp)
                .alpha(0f),
            enabled = !state.isAnswerEvaluated
        )

        Text(
            text = "İskelet Cümle (Yazmaya Başlayın):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SurvivalDanger
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = dynamicSkeleton,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SurvivalText,
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp // Karakterlerin ayrışması için geniş aralık
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        // Hata Geri Bildirim Metni (Görsel Animasyonlu)
        if (state.showErrorAnimation || errorAlpha > 0.01f) {
            Text(
                text = "Hatalı Tuş: ${state.wrongLetter}",
                color = SurvivalDanger,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .graphicsLayer(alpha = errorAlpha)
            )
        } else {
            // Boşlukta hizalama bozulmasın diye görünmez yer tutucu
            Spacer(modifier = Modifier.height(20.dp))
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
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurvivalSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎉 Tebrikler! 🎉",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurvivalPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aşama $stage Başarıyla Tamamlandı!",
                    fontSize = 16.sp,
                    color = SurvivalText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = SurvivalPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Sıradaki Aşamaya Geç ➡️",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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

fun toDynamicSkeletonText(target: String, userInput: String): String {
    val sb = java.lang.StringBuilder()
    for (i in target.indices) {
        val char = target[i]
        if (i < userInput.length) {
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
