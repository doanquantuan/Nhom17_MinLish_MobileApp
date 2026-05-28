package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minlish.model.VocabDeck
import com.example.minlish.ui.theme.*
import com.example.minlish.viewmodel.LearningViewModel

@Composable
fun WordSetListScreen(
    onNavigateToFlashcard: (String, Boolean) -> Unit,
    viewModel: LearningViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState()

    WordSetListContent(
        decks = decks,
        onNavigateToFlashcard = onNavigateToFlashcard
    )
}

@Composable
fun WordSetListContent(
    decks: List<VocabDeck>,
    onNavigateToFlashcard: (String, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            Text(
                "Chọn bộ từ để học",
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF534AB7)
            )
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
                    reviewCount = 17, // Sample count
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
                    onOnTap = { onNavigateToFlashcard(deck.id, true) }
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
fun DeckCard(deck: VocabDeck, onHocMoi: () -> Unit, onOnTap: () -> Unit) {
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
                        deck.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${deck.totalWords} từ · ${if (deck.wordsToReview > 0) "${deck.wordsToReview} cần ôn" else "Đã thuộc tốt"}",
                        color = if (deck.wordsToReview > 0) Color(0xFFFF9800) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEEEDFE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(deck.status, color = Color(0xFF534AB7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF27AE60),
                trackColor = Color(0xFFEEEEEE),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        deck = VocabDeck("1", "IELTS Academic", 120, 14, "IELTS", "#5A4FCF"),
        onHocMoi = {},
        onOnTap = {}
    )
}
