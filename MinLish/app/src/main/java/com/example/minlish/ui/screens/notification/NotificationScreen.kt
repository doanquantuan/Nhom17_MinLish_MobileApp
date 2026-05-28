package com.example.minlish.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.ui.screens.auth.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val primaryPurple = Color(0xFF534AB7)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Thông báo",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                // Add an empty action to help balance the centered title
                actions = { Spacer(modifier = Modifier.size(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryPurple)
            )
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Hôm nay")
            }

            item {
                NotificationItem(
                    title = "Đến giờ học rồi!",
                    description = "Bạn có 5 từ mới và 12 từ cần ôn hôm nay.",
                    time = "20:00",
                    dotColor = Color(0xFF534AB7)
                )
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            item {
                NotificationItem(
                    title = "14 từ đến hạn ôn",
                    description = "IELTS Academic có 14 từ sắp bị quên nếu không ôn hôm nay.",
                    time = "09:00",
                    dotColor = Color(0xFFE67E22)
                )
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader("Hôm qua")
            }

            item {
                NotificationItem(
                    title = "Streak 7 ngày!",
                    description = "Tuyệt vời! Bạn đã học liên tục 7 ngày rồi.",
                    time = "20:00",
                    dotColor = Color(0xFF27AE60)
                )
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            item {
                NotificationItem(
                    title = "Nhắc học hàng ngày",
                    description = "Đừng quên học từ vựng hôm nay nhé!",
                    time = "20:00",
                    dotColor = Color(0xFFD9D9D9)
                )
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontFamily = BeVietnamPro,
        fontSize = 16.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun NotificationItem(
    title: String,
    description: String,
    time: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 16.dp)
                .size(12.dp)
                .background(dotColor, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = BeVietnamPro,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontFamily = BeVietnamPro,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                fontFamily = BeVietnamPro,
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}
