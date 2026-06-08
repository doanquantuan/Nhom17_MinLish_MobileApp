package com.example.minlish.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinLishTopAppBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5145B1))
    )
}

@Composable
fun MinLishLargeHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    height: androidx.compose.ui.unit.Dp = 120.dp,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color(0xFF5145B1))
            .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(start = if (onBack == null) 8.dp else 0.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            
            Row(content = actions)
        }
    }
}
