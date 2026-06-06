package com.example.minlish.data.model

data class UserStats(
    val wordsLearned: Int = 0,
    val streak: Int = 0,
    val accuracy: Int = 0,
    val level: String = "..."
)

data class DailyActivity(
    val day: String, // T2, T3, T4...
    val wordsCount: Int,
    val isToday: Boolean = false
)

data class DeckRetention(
    val deckId: String = "",
    val deckName: String,
    val totalWords: Int,
    val retentionRate: Int,
    val tag: String = ""
)

data class DashboardData(
    val userStats: UserStats = UserStats(),
    val dailyPlan: DailyPlan = DailyPlan(),
    val wordSets: List<DeckRetention> = emptyList()
)

data class WordStatusDistribution(
    val newCount: Int = 0,
    val reviewCount: Int = 0,
    val masteredCount: Int = 0,
    val total: Int = 0
)

data class CategoryDistribution(
    val category: String,
    val wordCount: Int,
    val percentage: Float,
    val masteryRate: Int // % of mastered words in this category
)

data class TimeActivity(
    val period: String, // "Sáng", "Trưa", "Chiều", "Tối"
    val count: Int
)

data class StatisticsData(
    val weeklyActivity: List<DailyActivity> = emptyList(),
    val timeActivity: List<TimeActivity> = emptyList(),
    val categoryFocus: List<CategoryDistribution> = emptyList(),
    val statusDistribution: WordStatusDistribution = WordStatusDistribution(),
    val totalSessions: Int = 0,
    val totalStudyTime: String = "0h",
    val averageAccuracy: Int = 0,
    val currentStreak: Int = 0
)
