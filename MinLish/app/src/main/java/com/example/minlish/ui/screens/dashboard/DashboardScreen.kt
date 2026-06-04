package com.example.minlish.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.ui.components.RetentionRow
import com.example.minlish.ui.components.SimpleBarChart
import com.example.minlish.ui.components.StatCard
import com.example.minlish.ui.screens.auth.BeVietnamPro
import com.example.minlish.ui.screens.profile.ProfileScreen
import com.example.minlish.ui.screens.vocabulary.VocabularySetScreenContent
import com.example.minlish.ui.screens.vocabulary.WordSetListContent
import com.example.minlish.navigation.Routes
import com.example.minlish.viewmodel.AuthViewModel
import com.example.minlish.viewmodel.DashboardViewModel
import com.example.minlish.viewmodel.LearningViewModel
import com.example.minlish.viewmodel.VocabularyViewModel

@Composable
fun DashboardScreen(
    navController: NavController, 
    viewModel: DashboardViewModel = viewModel(),
    vocabViewModel: VocabularyViewModel = viewModel(),
    learningViewModel: LearningViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val primaryPurple = Color(0xFF534AB7)
    val dashboardData by viewModel.dashboardData.collectAsState()
    val statsData by viewModel.statsData.collectAsState()
    val isDashboardLoading by viewModel.isLoading.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val showReviewNotification by viewModel.showReviewNotification.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current

    // Observe review notification
    LaunchedEffect(showReviewNotification) {
        showReviewNotification?.let { count ->
            com.example.minlish.utils.NotificationHelper.showStudyReminder(
                context,
                "Ôn tập hàng ngày",
                "Hôm nay bạn có $count từ vựng cần ôn tập. Hãy bắt đầu ngay nhé!"
            )
            viewModel.clearNotificationEvent()
        }
    }
    
    // Auth state for name sync
    val userName = authViewModel.userName
    
    // Dashboard state refresh
    val isFinished by learningViewModel.isFinished.collectAsState()
    LaunchedEffect(isFinished) {
        if (isFinished) {
            android.util.Log.d("DashboardScreen", "SESSION FINISHED - TRIGGERING REFRESH")
            viewModel.refreshData()
            vocabViewModel.loadVocabularySets()
            learningViewModel.loadRealDecks()
        }
    }

    // Initial load and tab change load
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> {
                viewModel.refreshData()
            }
            1 -> vocabViewModel.loadVocabularySets()
            2 -> learningViewModel.loadRealDecks()
        }
    }
    
    // Vocab state
    val vocabularySets by vocabViewModel.vocabularySets.collectAsState()
    val setWordCounts by vocabViewModel.setWordCounts.collectAsState()
    val isVocabLoading by vocabViewModel.isLoading.collectAsState()
    
    // Learning state
    val filteredDecks by learningViewModel.filteredDecks.collectAsState()
    val filterMode by learningViewModel.filterMode.collectAsState()
    val totalToReview by learningViewModel.totalWordsToReview.collectAsState()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    if (isDashboardLoading && dashboardData.wordSets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryPurple)
                        }
                    } else {
                        HomeContent(
                            data = dashboardData,
                            primaryColor = primaryPurple,
                            displayName = userName,
                            onStartLearning = {
                                learningViewModel.startSession("all", false)
                                navController.navigate("flashcard")
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToDeck = { setId ->
                                navController.navigate(Routes.VocabularyList.passSetId(setId))
                            },
                            unreadCount = unreadCount
                        )
                    }
                }
                1 -> VocabularySetScreenContent(
                    vocabularySets = vocabularySets,
                    setWordCounts = setWordCounts,
                    isLoading = isVocabLoading,
                    onAddSetClick = { navController.navigate(Routes.CreateSet.route) },
                    onDeleteSet = { 
                        vocabViewModel.deleteVocabularySet(it)
                        viewModel.refreshData() 
                    },
                    onEditSet = { 
                        vocabViewModel.updateVocabularySet(it)
                        viewModel.refreshData()
                    },
                    onSetClick = { setId ->
                        navController.navigate(Routes.VocabularyList.passSetId(setId))
                    },
                    bottomBar = {} // BottomBar is managed by DashboardScreen
                )
                2 -> WordSetListContent(
                    decks = filteredDecks,
                    filterMode = filterMode,
                    totalToReview = totalToReview,
                    onFilterChange = { learningViewModel.setFilterMode(it) }
                ) { deckId, isReview ->
                    learningViewModel.startSession(deckId, isReview)
                    navController.navigate("flashcard")
                }
                3 -> StatisticsContent(statsData, primaryPurple)
                4 -> ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun HomeContent(
    data: com.example.minlish.model.DashboardData, 
    primaryColor: Color, 
    displayName: String,
    onStartLearning: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDeck: (String) -> Unit = {},
    unreadCount: Int = 0
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
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = primaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = displayName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                        Text(text = initials, color = primaryColor, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hôm nay", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = BeVietnamPro)
                        Text(text = data.dailyPlan, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = BeVietnamPro)
                    }
                    Button(
                        onClick = onStartLearning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Học ngay", color = Color.White, fontFamily = BeVietnamPro)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "TỪ ĐÃ HỌC", value = data.userStats.wordsLearned.toString(), modifier = Modifier.weight(1f), valueColor = primaryColor)
                StatCard(
                    label = "STREAK", 
                    value = data.userStats.streak.toString(), 
                    modifier = Modifier.weight(1f), 
                    valueColor = Color(0xFFE67E22),
                    icon = { Text("🔥") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "ACCURACY", value = "${data.userStats.accuracy}%", modifier = Modifier.weight(1f), valueColor = Color(0xFF27AE60))
                StatCard(label = "LEVEL", value = data.userStats.level, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(text = "Bộ từ của tôi", fontFamily = BeVietnamPro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(data.wordSets) { deck ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavigateToDeck(deck.deckId) },
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
    }
}

@Composable
fun StatisticsContent(data: com.example.minlish.model.StatisticsData, primaryColor: Color) {
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
                barColor = primaryColor
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(text = "Phân bổ trình độ từ vựng", fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            WordDistributionChart(data.statusDistribution, primaryColor)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(text = "Thời điểm học tập tích cực", fontFamily = BeVietnamPro, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            TimeActivityChart(data.timeActivity, primaryColor)
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
fun TimeActivityChart(data: List<com.example.minlish.model.TimeActivity>, primaryColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Tăng chiều cao để không bị che chữ
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val maxCount = data.maxOfOrNull { it.count } ?: 1
        data.forEach { activity ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxOf(4.dp, (70.dp * (activity.count.toFloat() / maxCount))))
                        .background(
                            if (activity.count == maxCount && activity.count > 0) primaryColor 
                            else primaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp)) // Tăng khoảng cách giữa cột và chữ
                Text(
                    text = activity.period, 
                    fontSize = 12.sp, 
                    fontFamily = BeVietnamPro, 
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WordDistributionChart(distribution: com.example.minlish.model.WordStatusDistribution, primaryColor: Color) {
    val total = if (distribution.total > 0) distribution.total.toFloat() else 1f
    
    val masteredWeight = distribution.masteredCount / total
    val reviewWeight = distribution.reviewCount / total
    val newWeight = distribution.newCount / total

    Column(modifier = Modifier.fillMaxWidth()) {
        // Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
        ) {
            if (masteredWeight > 0) {
                Box(modifier = Modifier.fillMaxHeight().weight(if (masteredWeight > 0) masteredWeight else 0.001f).background(Color(0xFF27AE60), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
            }
            if (reviewWeight > 0) {
                Box(modifier = Modifier.fillMaxHeight().weight(if (reviewWeight > 0) reviewWeight else 0.001f).background(Color(0xFFE67E22)))
            }
            if (newWeight > 0) {
                Box(modifier = Modifier.fillMaxHeight().weight(if (newWeight > 0) newWeight else 0.001f).background(primaryColor, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendItem("Đã thuộc", Color(0xFF27AE60), distribution.masteredCount)
            LegendItem("Đang ôn", Color(0xFFE67E22), distribution.reviewCount)
            LegendItem("Từ mới", primaryColor, distribution.newCount)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontFamily = BeVietnamPro, color = Color.Gray)
            Text(text = count.toString(), fontSize = 14.sp, fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold)
        }
    }
}
