package com.example.minlish.ui.screens.learning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
        var rotated by remember(currentIndex) { mutableStateOf(false) }
        
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
                    }
                    Text(
                        "${currentIndex + 1} / ${words.size} từ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
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
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / words.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(6.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
                    SrsRatingSection(onRate = { quality ->
                        viewModel.answerWord(quality)
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
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            word.meaning,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Example:",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryPurple,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            word.example,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            color = Color.DarkGray,
            modifier = Modifier.fillMaxWidth()
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
            SrsButton(Quality.AGAIN, "<1m", AgainButtonColor, ErrorRed, onRate)
            SrsButton(Quality.HARD, "1 ngày", HardButtonColor, AccentOrange, onRate)
            SrsButton(Quality.GOOD, "3 ngày", GoodButtonColor, SuccessGreen, onRate)
            SrsButton(Quality.EASY, "7 ngày", EasyButtonColor, PrimaryPurple, onRate)
        }
    }
}

@Composable
fun RowScope.SrsButton(
    quality: Quality,
    interval: String,
    bgColor: Color,
    textColor: Color,
    onRate: (Quality) -> Unit
) {
    Button(
        onClick = { onRate(quality) },
        modifier = Modifier
            .weight(1f)
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(quality.name.lowercase().replaceFirstChar { it.uppercase() }, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(interval, color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
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
