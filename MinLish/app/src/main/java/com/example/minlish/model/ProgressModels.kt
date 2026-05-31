package com.example.minlish.model

data class UserStats(
    val wordsLearned: Int = 0,
    val streak: Int = 0,
    val accuracy: Int = 0,
    val level: String = "..."
)

data class DailyActivity(
    val day: String, // T2, T3, T4...
    val wordsCount: Int
)

data class DeckRetention(
    val deckName: String,
    val totalWords: Int,
    val retentionRate: Int,
    val tag: String = ""
)

data class DashboardData(
    val userStats: UserStats = UserStats(),
    val dailyPlan: String = "Đang tải dữ liệu...",
    val wordSets: List<DeckRetention> = emptyList()
)

data class StatisticsData(
    val weeklyActivity: List<DailyActivity> = emptyList(),
    val retentionRates: List<DeckRetention> = emptyList(),
    val totalSessions: Int = 0,
    val totalStudyTime: String = "0h"
)
