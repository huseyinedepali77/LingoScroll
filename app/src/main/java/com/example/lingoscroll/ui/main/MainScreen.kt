package com.example.lingoscroll.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.lingoscroll.data.local.LearningCard
import com.example.lingoscroll.data.Level
import com.example.lingoscroll.theme.PastelGreen
import com.example.lingoscroll.theme.PastelRed
import com.example.lingoscroll.theme.PastelYellow

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
                        .padding(16.dp)
                ) {
                    WelcomeScreen(onStart = { viewModel.startStyleSelection() })
                }
            }
            is MainScreenUiState.OnboardingStyleSelection -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    StyleSelectionScreen(
                        onStyleSelected = { style -> viewModel.selectLearningStyle(style) }
                    )
                }
            }
            is MainScreenUiState.OnboardingQuiz -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    DiagnosticQuizScreen(
                        state = s,
                        onOptionSelected = { viewModel.answerDiagnosticQuestion(it) },
                        onNext = { viewModel.nextDiagnosticQuestion() }
                    )
                }
            }
            is MainScreenUiState.Practice -> {
                PracticeScreen(
                    state = s,
                    onRevealMeaning = { viewModel.revealCardMeaning() },
                    onEvaluateCard = { viewModel.evaluateCard(it) },
                    onAnswerQuiz = { viewModel.answerPracticeQuestion(it) },
                    onNext = { viewModel.nextPracticeItem() },
                    onSpeak = { viewModel.speakText(it) },
                    onReset = { viewModel.resetProgress() },
                    onOpenLearningCards = { viewModel.openLearningCards() },
                    onCloseLearningCards = { viewModel.closeLearningCards() },
                    onRevealLearningCard = { viewModel.revealLearningCard() },
                    onNextLearningCard = { viewModel.nextLearningCard(it) },
                    onStyleChange = { viewModel.changeLearningStyle(it) },
                    onSpeechRateChange = { viewModel.changeTtsSpeechRate(it) },
                    getStats = { viewModel.getStudyStats() },
                    onFeedUrlChange = { viewModel.updateFeedUrl(it) },
                    getFeedUrl = { viewModel.getCustomFeedUrl() }
                )
            }
        }
    }
}

// 1. Hoş Geldiniz Ekranı (OnboardingWelcome)
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LingoScroll",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sosyal medyada kaybolmak yerine, gün içindeki 5-10 dakikalık boşluklarında pratik İngilizce öğrenmeye hazır mısın?",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Neler Yapacağız?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• İlk adımda seviyeni belirleyen hızlı bir test çözeceksin.\n" +
                           "• Seviyene göre pratik günlük İngilizce kalıpları karşına çıkacak.\n" +
                           "• Hoparlör simgesiyle telaffuzları dinleyeceksin.\n" +
                           "• Akıllı yerel motorumuz öğrendiklerini hafızanda tutman için tekrarlarını yönetecek.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .heightIn(min = 52.dp)
        ) {
            Text(
                text = "Seviye Testini Başlat 🚀", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 1.5. Öğrenme Hedefi ve Tarz Seçimi Ekranı (OnboardingStyleSelection)
@Composable
fun StyleSelectionScreen(
    onStyleSelected: (String) -> Unit
) {
    var selected by remember { mutableStateOf("MIXED") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Öğrenme Hedefin Nedir? 🎯",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "İlgi alanına ve ihtiyacına en uygun İngilizce kalıpları belirleyelim.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            val options = listOf(
                StyleOption("TRAVEL", "✈️ Seyahat & Tatil", "Otel, restoran, yol tarifi, havalimanı ve tatil pratikleri."),
                StyleOption("BUSINESS", "💼 İş & Ofis", "İş toplantıları, resmi yazışmalar, sunumlar ve ofis deyimleri."),
                StyleOption("CASUAL", "💬 Günlük Yaşam", "Sokak dili, samimi diyaloglar ve en popüler günlük konuşma deyimleri."),
                StyleOption("MIXED", "🌀 Karışık Paket", "Yukarıdaki tüm alanların harmanlandığı All-in-One genel mod.")
            )

            options.forEach { option ->
                val isSelected = selected == option.id
                val borderStrokeColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                }
                val bgColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surface
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
                        .clickable { selected = option.id }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = option.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onStyleSelected(selected) },
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(50.dp)
        ) {
            Text("Seviye Sınavına Başla ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class StyleOption(val id: String, val title: String, val desc: String)

// 2. Seviye Tespit Sınavı Ekranı (OnboardingQuiz)
@Composable
fun DiagnosticQuizScreen(
    state: MainScreenUiState.OnboardingQuiz,
    onOptionSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val progress = (state.currentQuestionIndex.toFloat() + 1) / state.totalQuestions.toFloat()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fixed Top: Header & Progress
        Text(
            text = "Seviye Belirleme Sınavı",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${state.currentQuestionIndex + 1}/${state.totalQuestions}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Scrollable Body: Question & Options & Explanation
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.currentQuestion.expression,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 26.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Seçenekler
            state.currentQuestion.optionsList.forEach { option ->
                val isSelected = state.selectedOption == option
                val isCorrect = option == state.currentQuestion.correctAnswer
                val hasAnswered = state.selectedOption != null

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        hasAnswered && isCorrect -> PastelGreen
                        isSelected && !isCorrect -> PastelRed
                        else -> MaterialTheme.colorScheme.surface
                    },
                    label = "optionColor"
                )

                val borderStrokeColor = if (isSelected && !hasAnswered) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(1.dp, borderStrokeColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !hasAnswered) { onOptionSelected(option) }
                        .padding(14.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        color = if (hasAnswered && (isCorrect || isSelected)) Color(0xFF1E2321) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected || (hasAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = state.selectedOption != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Doğru Cevap: ${state.currentQuestion.correctAnswer}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.currentQuestion.example_sentence,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pinned Bottom: Continue button
        AnimatedVisibility(
            visible = state.selectedOption != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(50.dp)
            ) {
                Text("Devam Et", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. Günlük Pratik Ekranı (Practice)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    state: MainScreenUiState.Practice,
    onRevealMeaning: () -> Unit,
    onEvaluateCard: (Boolean) -> Unit,
    onAnswerQuiz: (String) -> Unit,
    onNext: () -> Unit,
    onSpeak: (String) -> Unit,
    onReset: () -> Unit,
    onOpenLearningCards: () -> Unit,
    onCloseLearningCards: () -> Unit,
    onRevealLearningCard: () -> Unit,
    onNextLearningCard: (Boolean) -> Unit,
    onStyleChange: (String) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    getStats: () -> StudyStats,
    onFeedUrlChange: (String) -> Unit,
    getFeedUrl: () -> String
) {
    var showStatsBottomSheet by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val statsSheetState = rememberModalBottomSheetState()
    val settingsSheetState = rememberModalBottomSheetState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "LingoScroll Menü 🌀",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                NavigationDrawerItem(
                    label = { Text("🃏 Günlük Kalıpları Keşfet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenLearningCards()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("📊 İstatistiklerim", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showStatsBottomSheet = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("⚙️ Ayarlar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsBottomSheet = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("🔄 İlerlemeyi Sıfırla", fontSize = 15.sp, color = PastelRed, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onReset()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Hamburger Button & Logo Top Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☰", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "LingoScroll",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // ⏱️ Günlük Zaman Sayacı (Dakika bazında)
                    val minutesSpent = state.secondsSpentToday / 60
                    val secondsSpent = state.secondsSpentToday % 60
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏱️ $minutesSpent dk",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Body: Card Content & Options
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Soru/Kart Kapsayıcısı (Pastel Card)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Bilgi Satırı ve Sesli Okuma
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.currentItem.type == "CARD") "GÜNLÜK KALIP" else "PRATİK TEST",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .clickable { onSpeak("") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🔊 Sesli Dinle",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Ana İçerik
                            Text(
                                text = state.currentItem.expression,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 28.sp
                            )

                            // QUIZ Tipi Türkçe Çeviri/İpucu Desteği
                            if (state.currentItem.type != "CARD") {
                                var showHint by remember(state.currentItem.id) { mutableStateOf(false) }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (showHint) {
                                    val hintText = remember(state.currentItem.id) {
                                        val rawHint = state.currentItem.example_sentence.ifEmpty { state.currentItem.translation }
                                        var cleanHint = rawHint
                                        state.currentItem.optionsList.forEach { option ->
                                            if (option.length > 2) {
                                                cleanHint = cleanHint.replace(option, "_____", ignoreCase = true)
                                            }
                                        }
                                        cleanHint.replace(state.currentItem.correctAnswer, "_____", ignoreCase = true)
                                    }
                                    Text(
                                        text = "İpucu / Açıklama: $hintText",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp
                                    )
                                } else {
                                    Text(
                                        text = "🔍 Türkçe Çevirisini Göster",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { showHint = true }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // CARD Tipi Açıklama Gösterme (Eğer feed'e düşerse)
                            if (state.currentItem.type == "CARD" && state.isMeaningRevealed) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = state.currentItem.translation,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.currentItem.example_sentence,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // QUIZ Tipi Seçenekler
                            if (state.currentItem.type != "CARD") {
                                state.currentItem.optionsList.forEach { option ->
                                    val isSelected = state.selectedOption == option
                                    val isCorrect = option == state.currentItem.correctAnswer
                                    val hasAnswered = state.isAnswerEvaluated

                                    val optionBgColor by animateColorAsState(
                                        targetValue = when {
                                            hasAnswered && isCorrect -> PastelGreen
                                            isSelected && !isCorrect -> PastelRed
                                            else -> MaterialTheme.colorScheme.background
                                        },
                                        label = "quizOptionColor"
                                    )

                                    val optionBorderColor = if (isSelected && !hasAnswered) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(optionBgColor)
                                            .border(1.dp, optionBorderColor, RoundedCornerShape(12.dp))
                                            .clickable(enabled = !hasAnswered) { onAnswerQuiz(option) }
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = option,
                                            fontSize = 15.sp,
                                            color = if (hasAnswered && (isCorrect || isSelected)) Color(0xFF1E2321) else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected || (hasAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pinned Bottom: Action Buttons (Sadece CARD tipleri için, QUIZ butonları popupta çıkacak)
                if (state.currentItem.type == "CARD") {
                    if (!state.isMeaningRevealed) {
                        Button(
                            onClick = onRevealMeaning,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .height(50.dp)
                        ) {
                            Text("Anlamını Göster", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Button(
                                onClick = { onEvaluateCard(false) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Review Again 🔁", color = Color(0xFF2C1919), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { onEvaluateCard(true) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Got It! 👍", color = Color(0xFF192C23), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 3.7. ÖN PLANDA MERKEZİ DEĞERLENDİRME POPUP'I (QUIZ Feedback Card)
            if (state.currentItem.type != "CARD" && state.isAnswerEvaluated) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {} // Arka plan tıklamalarını bloke et
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (state.isAnswerCorrect) "Tebrikler! 🎉" else "Hatalı Cevap 🥺",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isAnswerCorrect) Color(0xFF1D5A3F) else Color(0xFF752424)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isAnswerCorrect) "Harika gidiyorsun!" else "Tekrar deneyerek öğrenebilirsin.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Anlamı: ${state.currentItem.translation}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.currentItem.example_sentence,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // 🔊 Cümleyi Dinle Butonu (Cevap verdikten sonra doğru telaffuzu dinleme)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable { onSpeak("") }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔊 Cümleyi Dinle",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = onNext,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Sıradaki Kalıp ➡️", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3.5. Bağımsız Kelime Kartları Modalı (Flashcard Bottom Sheet)
    if (state.showLearningCards && state.learningCards.isNotEmpty()) {
        val learningSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = onCloseLearningCards,
            sheetState = learningSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val currentCard = state.learningCards.getOrNull(state.currentLearningCardIndex)
            if (currentCard != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Günlük Kalıp Kartları 🃏",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${state.currentLearningCardIndex + 1}/${state.learningCards.size}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Flashcard Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentCard.expression,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .clickable { onSpeak(currentCard.expression) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "🔊 Dinle",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Revealed details
                    if (state.isLearningCardRevealed) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentCard.translation,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCard.example_sentence,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Leitner evaluation buttons
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onNextLearningCard(false) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Yeniden Sor 🔁", color = Color(0xFF2C1919), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { onNextLearningCard(true) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Öğrendim! 👍", color = Color(0xFF192C23), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = onRevealLearningCard,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Anlamını Göster", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 4. İstatistikler Bottom Sheet
    if (showStatsBottomSheet) {
        val stats = getStats()
        ModalBottomSheet(
            onDismissRequest = { showStatsBottomSheet = false },
            sheetState = statsSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LingoScroll İstatistiklerim 📊",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                // İstatistik Kartları Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Seri Kartı
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Günlük Seri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("${stats.streak} Gün", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Çalışma Süresi Kartı
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⏱️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pratik Süresi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("${stats.totalMinutes} dk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Toplam Soru Kartı
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cevaplanan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("${stats.correctAnswered}/${stats.totalAnswered}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Doğruluk Oranı Kartı
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎯", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Doğruluk Oranı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("%${stats.accuracyPercent}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val levelText = when (state.currentLevel) {
                    Level.BEGINNER -> "Başlangıç Seviyesi (A1-A2)"
                    Level.INTERMEDIATE -> "Orta Seviye (B1-B2)"
                    Level.ADVANCED -> "İleri Seviye (C1)"
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mevcut Eğitim Düzeyi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Text(levelText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // 5. Ayarlar Bottom Sheet
    if (showSettingsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsBottomSheet = false },
            sheetState = settingsSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "LingoScroll Ayarları ⚙️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Öğrenme Tarzı Seçimi
                Text(
                    text = "Öğrenme Tarzı",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val styles = listOf(
                    "TRAVEL" to "✈️ Seyahat",
                    "BUSINESS" to "💼 İş",
                    "CASUAL" to "💬 Günlük",
                    "MIXED" to "🌀 Karışık"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styles.forEach { (styleKey, label) ->
                        val isSelected = state.selectedStyle == styleKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.background
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onStyleChange(styleKey) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Seslendirme Hızı Seçimi
                Text(
                    text = "🔊 Seslendirme Okuma Hızı",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val rates = listOf(
                    0.5f to "🐢 0.5x",
                    0.75f to "🚶 0.75x",
                    1.0f to "⚡ 1.0x"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rates.forEach { (rateValue, label) ->
                        val isSelected = state.speechRate == rateValue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.background
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSpeechRateChange(rateValue) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Özel Akış URL Girişi
                Text(
                    text = "🔗 Dinamik Senkronizasyon Bağlantısı (JSON URL)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                var feedUrlText by remember { mutableStateOf(getFeedUrl()) }

                androidx.compose.material3.OutlinedTextField(
                    value = feedUrlText,
                    onValueChange = { feedUrlText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    singleLine = true,
                    placeholder = { Text("https://example.com/feed.json", fontSize = 13.sp) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onFeedUrlChange(feedUrlText)
                        showSettingsBottomSheet = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Kaydet & Senkronize Et 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
