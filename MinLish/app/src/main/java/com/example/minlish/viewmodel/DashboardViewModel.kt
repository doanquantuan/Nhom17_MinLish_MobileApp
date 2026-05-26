package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minlish.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {
    private val _dashboardData = MutableStateFlow(
        DashboardData(
            userStats = UserStats(248, 7, 84, "B1-B2"),
            dailyPlan = "5 từ mới\n12 từ ôn",
            wordSets = listOf(
                DeckRetention("IELTS Academic", 72, "IELTS"),
                DeckRetention("Business Email", 90, "Business")
            )
        )
    )
    val dashboardData = _dashboardData.asStateFlow()

    private val _statsData = MutableStateFlow(
        StatisticsData(
            weeklyActivity = listOf(
                DailyActivity("T2", 10),
                DailyActivity("T3", 15),
                DailyActivity("T4", 8),
                DailyActivity("T5", 20),
                DailyActivity("T6", 25),
                DailyActivity("T7", 18),
                DailyActivity("CN", 12)
            ),
            retentionRates = listOf(
                DeckRetention("IELTS Academic", 72),
                DeckRetention("Business Email", 90),
                DeckRetention("Travel English", 48)
            ),
            totalSessions = 43,
            totalStudyTime = "12h"
        )
    )
    val statsData = _statsData.asStateFlow()
}
