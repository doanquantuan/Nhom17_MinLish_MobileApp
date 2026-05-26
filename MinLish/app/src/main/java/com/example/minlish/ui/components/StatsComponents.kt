package com.example.minlish.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.ui.screens.auth.BeVietnamPro

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF5F5F5),
    valueColor: Color = Color.Black,
    icon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontFamily = BeVietnamPro,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontFamily = BeVietnamPro,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            icon?.invoke()
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF534AB7)
) {
    val maxVal = if (data.isEmpty()) 1 else data.maxOrNull() ?: 1
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(100.dp * (value.toFloat() / maxVal))
                        .background(
                            color = if (index == data.size - 1) barColor else barColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = labels.getOrNull(index) ?: "",
                    fontSize = 10.sp,
                    fontFamily = BeVietnamPro,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RetentionRow(
    name: String,
    rate: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontFamily = BeVietnamPro, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "$rate%", fontFamily = BeVietnamPro, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { rate / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
