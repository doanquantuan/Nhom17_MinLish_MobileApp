package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.StudySession
import com.example.minlish.data.repository.StatsRepository
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.repository.VocabularySetRepository
import com.example.minlish.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val statsRepo = StatsRepository(db)
    private val vocabRepo = VocabularyRepository(db)
    private val setRepo = VocabularySetRepository(db)

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData = _dashboardData.asStateFlow()

    private val _statsData = MutableStateFlow(StatisticsData())
    val statsData = _statsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun refreshData() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessions = statsRepo.getSessionsByUserId(userId)
                val sets = setRepo.getSetsByUserId(userId)
                
                // Aggregate Stats
                val totalWordsLearned = sets.sumOf { vocabRepo.getWordCountBySet(it.id) }
                val streak = calculateStreak(sessions)
                val accuracy = calculateAccuracy(sessions)
                
                val userStats = UserStats(
                    wordsLearned = totalWordsLearned,
                    streak = streak,
                    accuracy = accuracy,
                    level = calculateLevel(totalWordsLearned)
                )

                val deckRetentions = sets.map { set ->
                    DeckRetention(
                        deckName = set.title,
                        retentionRate = set.progress,
                        tag = set.category
                    )
                }

                _dashboardData.value = DashboardData(
                    userStats = userStats,
                    wordSets = deckRetentions
                )

                // Weekly activity
                val weeklyActivity = calculateWeeklyActivity(sessions)
                
                _statsData.value = StatisticsData(
                    weeklyActivity = weeklyActivity,
                    retentionRates = deckRetentions,
                    totalSessions = sessions.size,
                    totalStudyTime = "${sessions.sumOf { it.durationMinutes }}m"
                )

            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error refreshing data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val sortedDates = sessions.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var currentCheck = today
        
        // If no session today, check if yesterday had one to maintain streak
        if (sortedDates.first() != today && sortedDates.first() != today - TimeUnit.DAYS.toMillis(1)) {
            return 0
        }

        for (date in sortedDates) {
            if (date == currentCheck || date == currentCheck - TimeUnit.DAYS.toMillis(1)) {
                streak++
                currentCheck = date
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateAccuracy(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val totalStudied = sessions.sumOf { it.cardsStudied }
        if (totalStudied == 0) return 0
        val totalCorrect = sessions.sumOf { it.correctAnswers }
        return (totalCorrect * 100) / totalStudied
    }

    private fun calculateLevel(wordsLearned: Int): String {
        return when {
            wordsLearned < 100 -> "A1-A2"
            wordsLearned < 500 -> "B1"
            wordsLearned < 1000 -> "B2"
            wordsLearned < 2000 -> "C1"
            else -> "C2"
        }
    }

    private fun calculateWeeklyActivity(sessions: List<StudySession>): List<DailyActivity> {
        val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
        
        // Map to T2..CN
        val dayIndexMap = mapOf(
            Calendar.MONDAY to 0,
            Calendar.TUESDAY to 1,
            Calendar.WEDNESDAY to 2,
            Calendar.THURSDAY to 3,
            Calendar.FRIDAY to 4,
            Calendar.SATURDAY to 5,
            Calendar.SUNDAY to 6
        )

        val last7DaysCounts = IntArray(7) { 0 }
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)

        sessions.filter { it.timestamp >= sevenDaysAgo }.forEach { session ->
            cal.timeInMillis = session.timestamp
            val dayIdx = dayIndexMap[cal.get(Calendar.DAY_OF_WEEK)] ?: 0
            last7DaysCounts[dayIdx] += session.cardsStudied
        }

        return days.mapIndexed { index, day ->
            DailyActivity(day, last7DaysCounts[index])
        }
    }
}
