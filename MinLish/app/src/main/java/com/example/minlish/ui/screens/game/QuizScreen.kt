package com.example.minlish.ui.screens.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController, 
    setId: String, 
    questionCount: Int = 10,
    viewModel: GameViewModel = viewModel()
) {
    val quizSession by viewModel.quizSession.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showReview by remember { mutableStateOf(false) }
    
    val primaryPurple = Color(0xFF534AB7)
    val lightBg = Color(0xFFF5F5F5)

    LaunchedEffect(setId, questionCount) {
        viewModel.startQuiz(setId, totalQuestions = questionCount, allowRepeat = false)
    }

    Scaffold(
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                showReview -> "Xem lại đáp án"
                                currentIndex >= (quizSession?.questions?.size ?: 0) -> "Kết quả"
                                quizSession != null -> "${currentIndex + 1} / ${quizSession!!.questions.size}"
                                else -> ""
                            },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BeVietnamPro
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showReview) showReview = false else navController.popBackStack() 
                    }) {
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
        } else if (quizSession != null && currentIndex < quizSession!!.questions.size) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Thanh tiến trình liền mạch
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / quizSession!!.questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF27AE60),
                    trackColor = primaryPurple.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Butt 
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Thẻ câu hỏi với chiều cao cố định để không bị nhảy vị trí
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CHỌN ĐÁP ÁN ĐÚNG",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BeVietnamPro
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = quizSession!!.questions[currentIndex].questionText,
                                color = Color.Black,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BeVietnamPro,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )

                            // Giữ chỗ cho loa để vị trí chữ không bị nhảy
                            Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                                if (quizSession!!.questions[currentIndex].direction == com.example.minlish.model.QuizDirection.EN_TO_VI) {
                                    IconButton(onClick = { viewModel.speak(quizSession!!.questions[currentIndex].questionText) }) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = primaryPurple, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CHỌN CÂU TRẢ LỜI",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BeVietnamPro,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        quizSession!!.questions[currentIndex].choices.forEachIndexed { index, choice ->
                            QuizChoiceItem(
                                label = ('A' + index).toString(),
                                text = choice,
                                onClick = { viewModel.onAnswerSelected(choice) },
                                primaryColor = primaryPurple
                            )
                        }
                    }
                }
            }
        } else if (quizSession != null && currentIndex >= quizSession!!.questions.size) {
            if (showReview) {
                QuizReviewContent(
                    session = quizSession!!,
                    modifier = Modifier.padding(padding)
                )
            } else {
                QuizResultContent(
                    score = quizSession!!.score,
                    total = quizSession!!.questions.size,
                    primaryColor = primaryPurple,
                    onReviewClick = { showReview = true },
                    onFinish = { navController.popBackStack() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun QuizReviewContent(
    session: com.example.minlish.model.QuizSession, 
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(session.questions) { index, question ->
            val userAnswer = session.userAnswers.getOrNull(index)
            val isCorrect = userAnswer == question.correctAnswer
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Câu ${index + 1}:",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontFamily = BeVietnamPro
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isCorrect) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF27AE60))
                        } else {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFE74C3C))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = question.questionText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = BeVietnamPro
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // User Answer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bạn chọn: ", fontSize = 14.sp, color = Color.Gray, fontFamily = BeVietnamPro)
                        Text(
                            text = userAnswer ?: "(Bỏ qua)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCorrect) Color(0xFF27AE60) else Color(0xFFE74C3C),
                            fontFamily = BeVietnamPro
                        )
                    }
                    
                    // Correct Answer if wrong
                    if (!isCorrect) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Đáp án đúng: ", fontSize = 14.sp, color = Color.Gray, fontFamily = BeVietnamPro)
                            Text(
                                text = question.correctAnswer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF27AE60),
                                fontFamily = BeVietnamPro
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizChoiceItem(label: String, text: String, onClick: () -> Unit, primaryColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, color = Color.DarkGray, fontSize = 16.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun QuizResultContent(
    score: Int, 
    total: Int, 
    primaryColor: Color, 
    onReviewClick: () -> Unit, 
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(primaryColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Hoàn thành!", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bạn đã đạt được $score / $total điểm", color = Color.Gray, fontSize = 18.sp, fontFamily = BeVietnamPro)
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Quay lại", color = Color.White, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onReviewClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor)
        ) {
            Text("Xem lại đáp án", color = primaryColor, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
        }
    }
}
