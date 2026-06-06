package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minlish.data.model.VocabularySet
import com.example.minlish.ui.theme.*
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.viewmodel.LearningViewModel
import com.example.minlish.viewmodel.DeckFilterMode

@Composable
fun WordSetListScreen(
    onNavigateToFlashcard: (String, Boolean) -> Unit,
    onNavigateToQuiz: (String, Int) -> Unit = { _, _ -> },
    onNavigateToMatching: (String, Int) -> Unit = { _, _ -> },
    viewModel: LearningViewModel = viewModel()
) {
    val decks by viewModel.filteredDecks.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val totalToReview by viewModel.totalWordsToReview.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showQuizDialog by remember { mutableStateOf(false) }
    var showMatchingDialog by remember { mutableStateOf(false) }
    var selectedDeckForGame by remember { mutableStateOf<VocabularySet?>(null) }

    if (showQuizDialog && selectedDeckForGame != null) {
        val maxQuestions = selectedDeckForGame!!.wordsLearned + selectedDeckForGame!!.wordsToReview
        if (maxQuestions > 0) {
            GameConfigDialog(
                title = "Cấu hình bài Quiz",
                maxQuestions = maxQuestions,
                onDismiss = { showQuizDialog = false },
                onConfirm = { count ->
                    showQuizDialog = false
                    onNavigateToQuiz(selectedDeckForGame!!.id, count)
                }
            )
        } else {
            // Thông báo nếu chưa học từ nào
            LaunchedEffect(showQuizDialog) {
                android.widget.Toast.makeText(context, "Vui lòng học từ mới trước khi làm Quiz!", android.widget.Toast.LENGTH_SHORT).show()
                showQuizDialog = false
            }
        }
    }

    if (showMatchingDialog && selectedDeckForGame != null) {
        val maxQuestions = selectedDeckForGame!!.wordsLearned + selectedDeckForGame!!.wordsToReview
        if (maxQuestions > 0) {
            GameConfigDialog(
                title = "Cấu hình trò chơi nối thẻ",
                maxQuestions = maxQuestions,
                onDismiss = { showMatchingDialog = false },
                onConfirm = { count ->
                    showMatchingDialog = false
                    onNavigateToMatching(selectedDeckForGame!!.id, count)
                }
            )
        } else {
            // Thông báo nếu chưa học từ nào
            LaunchedEffect(showMatchingDialog) {
                android.widget.Toast.makeText(context, "Vui lòng học từ mới trước khi chơi nối thẻ!", android.widget.Toast.LENGTH_SHORT).show()
                showMatchingDialog = false
            }
        }
    }

    WordSetListContent(
        decks = decks,
        filterMode = filterMode,
        totalToReview = totalToReview,
        onFilterChange = { viewModel.setFilterMode(it) },
        onNavigateToFlashcard = onNavigateToFlashcard,
        onQuizClick = { deck ->
            selectedDeckForGame = deck
            showQuizDialog = true
        },
        onMatchingClick = { deck ->
            selectedDeckForGame = deck
            showMatchingDialog = true
        }
    )
}

@Composable
fun GameConfigDialog(
    title: String,
    maxQuestions: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var count by remember { mutableFloatStateOf(minOf(10f, maxQuestions.toFloat())) }
    val primaryPurple = Color(0xFF534AB7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    "Chọn số lượng từ (tối đa $maxQuestions):",
                    fontFamily = BeVietnamPro,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "${count.toInt()} từ vựng",
                    fontFamily = BeVietnamPro,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryPurple,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Slider(
                    value = count,
                    onValueChange = { count = it },
                    valueRange = 1f..maxQuestions.toFloat(),
                    steps = if (maxQuestions > 1) maxQuestions - 2 else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryPurple,
                        activeTrackColor = primaryPurple,
                        inactiveTrackColor = primaryPurple.copy(alpha = 0.2f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(count.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Bắt đầu học", fontFamily = BeVietnamPro)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray, fontFamily = BeVietnamPro)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun WordSetListContent(
    decks: List<VocabularySet>,
    filterMode: DeckFilterMode,
    totalToReview: Int,
    onFilterChange: (DeckFilterMode) -> Unit,
    onNavigateToFlashcard: (String, Boolean) -> Unit,
    onQuizClick: (VocabularySet) -> Unit = {},
    onMatchingClick: (VocabularySet) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Học tập",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF534AB7)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterMode == DeckFilterMode.LEARN_NEW,
                        onClick = { onFilterChange(DeckFilterMode.LEARN_NEW) },
                        label = { Text("Học mới") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF534AB7),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = filterMode == DeckFilterMode.REVIEW,
                        onClick = { onFilterChange(DeckFilterMode.REVIEW) },
                        label = { Text("Ôn tập") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF534AB7),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = filterMode == DeckFilterMode.ALL,
                        onClick = { onFilterChange(DeckFilterMode.ALL) },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF534AB7),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DailyPlanCard(
                    reviewCount = totalToReview,
                    onReviewNow = { onNavigateToFlashcard("all", true) }
                )
            }

            item {
                Text(
                    "Bộ từ của tôi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(decks) { deck ->
                DeckCard(
                    deck = deck,
                    onHocMoi = { onNavigateToFlashcard(deck.id, false) },
                    onOnTap = { onNavigateToFlashcard(deck.id, true) },
                    onQuizClick = { onQuizClick(deck) },
                    onMatchingClick = { onMatchingClick(deck) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DailyPlanCard(reviewCount: Int, onReviewNow: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Ôn tập hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$reviewCount thẻ đến hạn ôn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE65100)
                )
            }
            Button(
                onClick = onReviewNow,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ôn ngay")
            }
        }
    }
}

@Composable
fun DeckCard(
    deck: VocabularySet, 
    onHocMoi: () -> Unit, 
    onOnTap: () -> Unit,
    onQuizClick: () -> Unit = {},
    onMatchingClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        deck.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val progressPercent = if (deck.totalWords > 0) (deck.wordsLearned * 100 / deck.totalWords) else 0
                    val statusText = "${deck.wordsLearned} từ - $progressPercent% thuộc"

                    Text(
                        statusText,
                        color = if (deck.wordsLearned > 0 || deck.wordsToReview > 0) Color(0xFF4CAF50) else Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEEDFE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(deck.category, color = Color(0xFF534AB7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val progress = if (deck.totalWords > 0) deck.wordsLearned.toFloat() / deck.totalWords else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF27AE60),
                trackColor = Color(0xFFEEEEEE),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onHocMoi,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF534AB7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Học mới")
                }
                OutlinedButton(
                    onClick = onOnTap,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF534AB7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ôn tập", color = Color(0xFF534AB7))
                }
                
                IconButton(
                    onClick = onQuizClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz, 
                        contentDescription = "Quiz", 
                        tint = Color(0xFF534AB7),
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(
                    onClick = onMatchingClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension, 
                        contentDescription = "Matching", 
                        tint = Color(0xFF534AB7),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyPlanCardPreview() {
    DailyPlanCard(reviewCount = 17, onReviewNow = {})
}

@Preview(showBackground = true)
@Composable
fun DeckCardPreview() {
    DeckCard(
        deck = VocabularySet(id = "1", title = "IELTS Academic", category = "IELTS", totalWords = 120, wordsLearned = 45, wordsToReview = 14),
        onHocMoi = {},
        onOnTap = {}
    )
}
