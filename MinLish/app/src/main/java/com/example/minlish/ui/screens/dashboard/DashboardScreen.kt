package com.example.minlish.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.ui.screens.profile.ProfileScreen // Đảm bảo import đúng file profile của bạn
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    // Biến lưu vị trí nút đang được bấm (0: Trang chủ, ..., 4: Cá nhân)
    var selectedTab by remember { mutableIntStateOf(0) }
    val primaryPurple = Color(0xFF534AB7)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Trang chủ", fontFamily = BeVietnamPro) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, selectedTextColor = primaryPurple)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Bộ từ", fontFamily = BeVietnamPro) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, selectedTextColor = primaryPurple)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("Học", fontFamily = BeVietnamPro) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, selectedTextColor = primaryPurple)
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Thống kê", fontFamily = BeVietnamPro) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, selectedTextColor = primaryPurple)
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Cá nhân", fontFamily = BeVietnamPro) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, selectedTextColor = primaryPurple)
                )
            }
        }
    ) { innerPadding ->
        // Vùng hiển thị nội dung động thay đổi theo nút bấm
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giao diện Trang chủ sẽ viết ở đây") }
                1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giao diện Bộ từ") }
                2 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giao diện Học từ vựng") }
                3 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giao diện Thống kê") }

                // NÚT THỨ 5: GỌI TRANG PROFILE SỬ DỤNG CHUNG KHUNG BOTTOM BAR
                4 -> ProfileScreen(navController)
            }
        }
    }
}