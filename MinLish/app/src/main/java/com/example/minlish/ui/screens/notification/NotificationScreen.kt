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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minlish.data.model.Notification
import com.example.minlish.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val primaryPurple = Color(0xFF534AB7)
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val groupedNotifications = remember(notifications) {
        groupNotifications(notifications)
    }

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
                actions = { Spacer(modifier = Modifier.size(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryPurple)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryPurple
                )
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Không có thông báo nào",
                        fontFamily = BeVietnamPro,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    groupedNotifications.forEach { (header, items) ->
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionHeader(header)
                        }

                        items(items) { notification ->
                            NotificationItem(
                                title = notification.title,
                                description = notification.description,
                                time = formatTimestamp(notification.timestamp),
                                dotColor = getNotificationColor(notification.type),
                                isRead = notification.isRead,
                                onClick = { viewModel.markAsRead(notification.id) }
                            )
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

private fun groupNotifications(notifications: List<Notification>): Map<String, List<Notification>> {
    val calendar = Calendar.getInstance()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterday = today - 24 * 60 * 60 * 1000

    return notifications.groupBy {
        when {
            it.timestamp >= today -> "Hôm nay"
            it.timestamp >= yesterday -> "Hôm qua"
            else -> "Cũ hơn"
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getNotificationColor(type: String): Color {
    // Luôn trả về màu xanh lá cho thông báo chưa đọc để khớp với yêu cầu user
    return Color(0xFF27AE60)
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
    dotColor: Color,
    isRead: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
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
                    .background(if (isRead) Color.Transparent else dotColor, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = BeVietnamPro,
                    fontSize = 18.sp,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
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
}

