package com.example.minlish.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.data.model.DashboardData
import com.example.minlish.data.model.DeckRetention
import com.example.minlish.ui.components.StatCard
import com.example.minlish.ui.screens.auth.BeVietnamPro

@Composable
fun HomeScreen(
    data: DashboardData,
    displayName: String,
    unreadCount: Int,
    primaryColor: Color,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDeck: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Chào buổi sáng", fontFamily = BeVietnamPro, fontSize = 14.sp, color = Color.Gray)
                    Text(text = "$displayName 👋", fontFamily = BeVietnamPro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NotificationBadge(unreadCount, primaryColor, onNavigateToNotifications)
                    Spacer(modifier = Modifier.width(8.dp))
                    UserAvatar(displayName, primaryColor)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            UserStatsGrid(data, primaryColor)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(text = "Bộ từ của tôi", fontFamily = BeVietnamPro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(data.wordSets) { deck ->
            HomeDeckCard(deck, primaryColor, onNavigateToDeck)
        }
    }
}

@Composable
fun NotificationBadge(unreadCount: Int, primaryColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = primaryColor)
        }
    }
}

@Composable
fun UserAvatar(displayName: String, primaryColor: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(primaryColor.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val initials = displayName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull() }
            .joinToString("")
            .take(2)
            .uppercase()
        Text(text = initials.ifEmpty { "ME" }, color = primaryColor, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
    }
}

@Composable
fun UserStatsGrid(data: DashboardData, primaryColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "TỪ ĐÃ HỌC", value = data.userStats.wordsLearned.toString(), modifier = Modifier.weight(1f), valueColor = primaryColor)
            StatCard(
                label = "CHUỖI HỌC",
                value = data.userStats.streak.toString(),
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFFE67E22),
                icon = { Text("🔥") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "CHÍNH XÁC", value = "${data.userStats.accuracy}%", modifier = Modifier.weight(1f), valueColor = Color(0xFF27AE60))
            StatCard(label = "CẤP ĐỘ", value = data.userStats.level, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun HomeDeckCard(deck: DeckRetention, primaryColor: Color, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick(deck.deckId) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deck.deckName, fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "${deck.totalWords} từ · ${deck.retentionRate}% thuộc", fontFamily = BeVietnamPro, fontSize = 12.sp, color = Color.Gray)
            }
            if (deck.tag.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = deck.tag, color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
                }
            }
        }
    }
}
