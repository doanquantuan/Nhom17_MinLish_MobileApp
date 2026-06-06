package com.example.minlish.ui.screens.learning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.minlish.model.Quality
import com.example.minlish.model.VocabWord
import com.example.minlish.ui.theme.*
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.viewmodel.LearningViewModel

@Composable
fun FlashcardScreen(
    viewModel: LearningViewModel,
    onBack: () -> Unit
) {
    val words by viewModel.currentSessionWords.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isLoading by viewModel.isLoadingSession.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(PrimaryPurple), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else if (words.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(PrimaryPurple), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(
                    "Hiện tại không có từ nào trong mục này.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quay lại", color = PrimaryPurple)
                }
            }
        }
    } else if (currentIndex < words.size) {
        val currentWord = words[currentIndex]
        
        key(currentIndex) {
            var rotated by remember { mutableStateOf(false) }

            val rotation by animateFloatAsState(
                targetValue = if (rotated) 180f else 0f,
                animationSpec = tween(durationMillis = 500),
                label = "CardRotation"
            )

            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding() // Thêm padding cho thanh trạng thái
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.endSessionEarly()
                            onBack()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                        }
                        Text(
                            "${currentIndex + 1} / ${words.size} từ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = BeVietnamPro
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                containerColor = PrimaryPurple
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    SegmentedProgressIndicator(
                        currentIndex = currentIndex,
                        totalCount = words.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable { rotated = !rotated },
                    ) {
                        if (rotation <= 90f) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FlashcardFront(currentWord)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationY = 180f
                                    },
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FlashcardBack(currentWord)
                                }
                            }
                        }
                    }

                    if (rotated) {
                        var isProcessing by remember { mutableStateOf(false) }
                        SrsRatingSection(onRate = { quality ->
                            if (!isProcessing) {
                                isProcessing = true
                                viewModel.answerWord(quality)
                            }
                        })
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Bạn có nhớ từ này không?",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { rotated = true },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Lật thẻ", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FlashcardFront(word: VocabWord) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            word.word,
            style = MaterialTheme.typography.displayMedium,
            color = PrimaryPurple,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun FlashcardBack(word: VocabWord) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        FlashcardField(label = "Word", value = word.word)
        FlashcardField(label = "Meaning", value = word.meaning)
        FlashcardField(label = "Type", value = word.partOfSpeech)
        FlashcardField(label = "Pronunciation", value = word.phonetic)
        FlashcardField(label = "Example", value = word.example)
        FlashcardField(label = "Collocation", value = word.collocation)
        FlashcardField(label = "Note", value = word.note)
    }
}

@Composable
fun FlashcardField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.titleSmall,
            color = PrimaryPurple,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (value.isBlank()) "---" else value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SrsRatingSection(onRate: (Quality) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đánh giá mức độ nhớ:", color = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SrsButton(Quality.AGAIN, AgainButtonColor, ErrorRed, onRate)
            SrsButton(Quality.HARD, HardButtonColor, AccentOrange, onRate)
            SrsButton(Quality.GOOD, GoodButtonColor, SuccessGreen, onRate)
            SrsButton(Quality.EASY, EasyButtonColor, PrimaryPurple, onRate)
        }
    }
}

@Composable
fun RowScope.SrsButton(
    quality: Quality,
    bgColor: Color,
    textColor: Color,
    onRate: (Quality) -> Unit
) {
    Button(
        onClick = { onRate(quality) },
        modifier = Modifier
            .weight(1f)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            quality.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SegmentedProgressIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0xFFC0C0C0).copy(alpha = 0.4f) // Silver-ish
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until totalCount) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = if (i <= currentIndex) activeColor else inactiveColor,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreview() {
    FlashcardFront(VocabWord(
        word = "ambiguous",
        phonetic = "/æmˈbɪɡjuəs/",
        partOfSpeech = "adjective"
    ))
}
