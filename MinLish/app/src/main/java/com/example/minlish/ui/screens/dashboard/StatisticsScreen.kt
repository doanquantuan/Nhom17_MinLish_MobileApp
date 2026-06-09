package com.example.minlish.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.data.model.StatisticsData
import com.example.minlish.data.model.TimeActivity
import com.example.minlish.data.model.WordStatusDistribution
import com.example.minlish.ui.components.SimpleBarChart
import com.example.minlish.ui.components.StatCard
import com.example.minlish.ui.screens.auth.BeVietnamPro

@Composable
fun StatisticsScreen(data: StatisticsData, primaryColor: Color) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(text = "Thống kê", fontFamily = BeVietnamPro, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(text = "Hoạt động 7 ngày qua", fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            SimpleBarChart(
                data = data.weeklyActivity.map { it.wordsCount },
                labels = data.weeklyActivity.map { it.day },
                barColor = primaryColor,
                highlightIndex = data.weeklyActivity.indexOfFirst { it.isToday == true }.takeIf { it != -1 }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(text = "Phân bổ trình độ từ vựng", fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            DistributionChart(data.statusDistribution, primaryColor)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(text = "Thời điểm học tập tích cực", fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            ActivityTimeChart(data.timeActivity, primaryColor)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "TỔNG PHIÊN HỌC",
                    value = data.totalSessions.toString(),
                    modifier = Modifier.weight(1f),
                    backgroundColor = primaryColor.copy(alpha = 0.05f),
                    valueColor = primaryColor
                )
                StatCard(
                    label = "THỜI GIAN HỌC",
                    value = data.totalStudyTime,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF2980B9).copy(alpha = 0.05f),
                    valueColor = Color(0xFF2980B9)
                )
            }
        }
    }
}

@Composable
private fun ActivityTimeChart(data: List<TimeActivity>, primaryColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val maxCount = data.maxOfOrNull { it.count } ?: 1
        data.forEach { activity ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(maxOf(4.dp, (70.dp * (activity.count.toFloat() / maxCount))))
                        .background(if (activity.count == maxCount && activity.count > 0) primaryColor else primaryColor.copy(alpha = 0.3f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = activity.period, fontSize = 12.sp, fontFamily = BeVietnamPro, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun DistributionChart(distribution: WordStatusDistribution, primaryColor: Color) {
    val total = if (distribution.total > 0) distribution.total.toFloat() else 1f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(24.dp).background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))) {
            if (distribution.masteredCount > 0) Box(modifier = Modifier.fillMaxHeight().weight(distribution.masteredCount/total).background(Color(0xFF27AE60), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
            if (distribution.reviewCount > 0) Box(modifier = Modifier.fillMaxHeight().weight(distribution.reviewCount/total).background(Color(0xFFE67E22)))
            if (distribution.newCount > 0) Box(modifier = Modifier.fillMaxHeight().weight(distribution.newCount/total).background(primaryColor, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Legend(label = "Đã thuộc", color = Color(0xFF27AE60), count = distribution.masteredCount)
            Legend(label = "Đang ôn", color = Color(0xFFE67E22), count = distribution.reviewCount)
            Legend(label = "Từ mới", color = primaryColor, count = distribution.newCount)
        }
    }
}

@Composable
private fun Legend(label: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontFamily = BeVietnamPro, color = Color.Gray)
            Text(text = count.toString(), fontSize = 14.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
        }
    }
}
