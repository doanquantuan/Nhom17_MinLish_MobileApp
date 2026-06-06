package com.example.minlish.ui.screens.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.model.MatchingCard
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    navController: NavController, 
    setId: String, 
    questionCount: Int = 10,
    viewModel: GameViewModel = viewModel()
) {
    val matchingSession by viewModel.matchingSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val primaryPurple = Color(0xFF534AB7)
    val lightBg = Color(0xFFF5F5F5)

    LaunchedEffect(setId, questionCount) {
        viewModel.startMatching(setId, totalPairs = questionCount)
    }

    Scaffold(
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (matchingSession?.isFinished == true) "Kết quả" else "Nối thẻ", 
                            color = Color.White, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            fontFamily = BeVietnamPro
                        )
                        if (matchingSession != null && !matchingSession!!.isFinished) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${matchingSession?.totalMatchesFound ?: 0}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Đúng", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "${matchingSession?.totalErrors ?: 0}", color = Color(0xFFFFCDD2), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Sai", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = { Spacer(modifier = Modifier.size(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryPurple)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryPurple)
            }
        } else if (matchingSession != null) {
            if (matchingSession!!.isFinished) {
                MatchingResultContent(
                    matches = matchingSession!!.totalMatchesFound,
                    errors = matchingSession!!.totalErrors,
                    startTime = matchingSession!!.startTime,
                    primaryColor = primaryPurple,
                    onFinish = { navController.popBackStack() },
                    modifier = Modifier.padding(padding)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(matchingSession!!.currentRoundCards) { card ->
                            MatchingCardItem(card, primaryPurple) {
                                viewModel.onCardClicked(card)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchingResultContent(
    matches: Int,
    errors: Int,
    startTime: Long,
    primaryColor: Color,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMs = System.currentTimeMillis() - startTime
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val timeStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Dãn đều nội dung
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Box(
                modifier = Modifier.size(140.dp).background(primaryColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(80.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Tuyệt vời!", color = Color.Black, fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
            Text("Bạn đã hoàn thành trò chơi", color = Color.Gray, fontSize = 16.sp, fontFamily = BeVietnamPro)
        }
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "THỐNG KÊ CHI TIẾT", 
                color = Color.Gray, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold, 
                fontFamily = BeVietnamPro,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResultStatCard("Đúng", matches.toString(), Color(0xFF27AE60), Modifier.weight(1f))
                ResultStatCard("Sai", errors.toString(), Color(0xFFE74C3C), Modifier.weight(1f))
                ResultStatCard("Thời gian", timeStr, primaryColor, Modifier.weight(1.2f))
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Quay lại màn hình học", color = Color.White, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ResultStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 12.sp, color = Color.Gray, fontFamily = BeVietnamPro)
        }
    }
}

@Composable
fun MatchingCardItem(card: MatchingCard, primaryColor: Color, onClick: () -> Unit) {
    val backgroundColor = when {
        card.isMatched -> Color(0xFF27AE60)
        card.isSelected -> primaryColor
        card.isError -> Color(0xFFE74C3C)
        else -> Color.White
    }

    val textColor = if (card.isSelected || card.isMatched || card.isError) Color.White else Color.Black
    val elevation = if (card.isSelected || card.isMatched) 0.dp else 4.dp

    // Tính toán kích thước font dựa trên độ dài của text
    // Tiếng Anh (EN) ưu tiên 1 dòng, Tiếng Việt (VI) cho phép 2 dòng
    val fontSize = when {
        card.text.length > 20 -> 11.sp
        card.text.length > 12 -> 13.sp
        else -> 15.sp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f) 
            .clickable(enabled = !card.isMatched) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (card.isMatched || card.isSelected || card.isError) null 
                 else androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEEEEEE))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = BeVietnamPro,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2, // Cho phép tối đa 2 dòng cho nghĩa tiếng Việt
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
