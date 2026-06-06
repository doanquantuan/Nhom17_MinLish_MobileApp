package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.StudySession
import com.example.minlish.data.repository.NotificationRepository
import com.example.minlish.data.repository.StatsRepository
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.repository.VocabularySetRepository
import com.example.minlish.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val statsRepo = StatsRepository(db)
    private val vocabRepo = VocabularyRepository(db)
    private val setRepo = VocabularySetRepository(db)
    private val notificationRepo = NotificationRepository(db)

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData = _dashboardData.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val _statsData = MutableStateFlow(StatisticsData())
    val statsData = _statsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _showReviewNotification = MutableStateFlow<Int?>(null)
    val showReviewNotification = _showReviewNotification.asStateFlow()

    private var hasShownNotificationThisSession = false

    private var unreadCountJob: Job? = null

    init {
        refreshData()
        observeUnreadCount()
    }

    private fun observeUnreadCount() {
        val userId = auth.currentUser?.uid ?: return
        android.util.Log.d("DashboardVM", "Starting unread count observer for user: $userId")
        unreadCountJob?.cancel()
        unreadCountJob = viewModelScope.launch {
            notificationRepo.getUnreadCountFlow(userId).collectLatest { count ->
                android.util.Log.d("DashboardVM", "Observed unread count: $count")
                _unreadCount.value = count
            }
        }
    }

    fun refreshData() {
        val userId = auth.currentUser?.uid ?: return
        android.util.Log.d("DashboardVM", "Refreshing dashboard for user: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessionsDeferred = async { statsRepo.getSessionsByUserId(userId) }
                val setsDeferred = async { setRepo.getSetsByUserId(userId) }
                val allWordsDeferred = async { vocabRepo.getAllWordsByUserId(userId) }
                val userDocDeferred = async { db.collection("users").document(userId).get().await() }

                val sessions = sessionsDeferred.await()
                val sets = setsDeferred.await()
                val allWordsRaw = allWordsDeferred.await()
                val allWords = allWordsRaw.distinctBy { it.id }
                val userDoc = userDocDeferred.await()
                
                android.util.Log.d("DashboardVM", "REFRESHING: Found ${sets.size} sets and ${allWords.size} unique words (from ${allWordsRaw.size} raw)")
                allWords.forEach { 
                    if (it.repetitions > 0) {
                        android.util.Log.d("DashboardVM", "WORD STATUS: ${it.word} | STATUS: ${it.status} | REPS: ${it.repetitions}")
                    }
                }
                var userGoal = 10
                if (userDoc.exists()) {
                    userGoal = userDoc.getLong("wordsPerDay")?.toInt() ?: 10
                }

                // Calculate Daily Progress
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                val now = System.currentTimeMillis()

                android.util.Log.d("DashboardVM", "DATE RANGE: startOfToday=$startOfToday, now=$now")
                
                val newLearnedToday = allWords.count { 
                    val fr = it.firstReviewedAt
                    val isNewToday = fr != null && fr >= startOfToday
                    if (isNewToday) {
                        android.util.Log.d("DashboardVM", "COUNTED AS NEW TODAY: ${it.word} (ID: ${it.id}) firstReviewedAt=$fr")
                    }
                    isNewToday
                }
                
                val dueToReview = allWords.count {
                    it.repetitions > 0 && it.nextReview <= now
                }

                val reviewedToday = allWords.count {
                    val lr = it.lastReviewed
                    val fr = it.firstReviewedAt
                    lr != null && lr >= startOfToday && fr != null && fr < startOfToday
                }

                val totalReviewDue = reviewedToday + dueToReview
                
                // Aggregate Stats
                val totalWordsLearned = allWords.count { it.status == "Thuộc" }
                val streak = calculateStreak(sessions)
                val accuracy = calculateAccuracy(sessions)
                
                val userStats = UserStats(
                    wordsLearned = totalWordsLearned,
                    streak = streak,
                    accuracy = accuracy,
                    level = calculateLevel(totalWordsLearned)
                )

                val deckRetentions = sets.map { set ->
                    val setWords = allWords.filter { it.setId == set.id }
                    val learnedInSet = setWords.count { it.status == "Thuộc" }
                    val retention = if (setWords.isNotEmpty()) (learnedInSet * 100 / setWords.size) else 0
                    
                    DeckRetention(
                        deckId = set.id,
                        deckName = set.title,
                        totalWords = setWords.size,
                        retentionRate = retention,
                        tag = set.category
                    )
                }

                _dashboardData.value = DashboardData(
                    userStats = userStats,
                    dailyPlan = DailyPlan(
                        newWordsTarget = userGoal,
                        newWordsLearned = newLearnedToday,
                        reviewWordsCount = totalReviewDue,
                        reviewWordsDone = reviewedToday,
                        streak = streak,
                        accuracy = accuracy.toDouble()
                    ),
                    wordSets = deckRetentions
                )

                if (!hasShownNotificationThisSession && dueToReview > 0) {
                    sendReviewNotification(userId, dueToReview)
                }

                // Weekly activity
                val weeklyActivity = calculateWeeklyActivity(sessions)
                val timeActivity = calculateTimeActivity(sessions)
                
                // Category Focus with Mastery Rate
                val categoryFocus = sets.groupBy { it.category }.map { (category, categorySets) ->
                    val setIds = categorySets.map { it.id }
                    val categoryWords = allWords.filter { it.setId in setIds }
                    val wordCount = categoryWords.size
                    val masteredInCat = categoryWords.count { it.status == "Thuộc" || it.status == "Thuoc" }
                    val masteryRate = if (wordCount > 0) (masteredInCat * 100 / wordCount) else 0
                    
                    CategoryDistribution(
                        category = category.ifEmpty { "Chưa phân loại" },
                        wordCount = wordCount,
                        percentage = if (allWords.isNotEmpty()) (wordCount.toFloat() / allWords.size) else 0f,
                        masteryRate = masteryRate
                    )
                }.sortedByDescending { it.wordCount }

                // Word Status Distribution
                val distribution = WordStatusDistribution(
                    newCount = allWords.count { it.status == "Mới" || it.status == "Moi" },
                    reviewCount = allWords.count { it.status == "Ôn lại" || it.status == "On lai" },
                    masteredCount = allWords.count { it.status == "Thuộc" || it.status == "Thuoc" },
                    total = allWords.size
                )
                
                _statsData.value = StatisticsData(
                    weeklyActivity = weeklyActivity,
                    timeActivity = timeActivity,
                    categoryFocus = categoryFocus,
                    statusDistribution = distribution,
                    totalSessions = sessions.size,
                    totalStudyTime = "${sessions.sumOf { it.durationMinutes }}m",
                    averageAccuracy = accuracy,
                    currentStreak = streak
                )

            } catch (e: Exception) {
                if (e is CancellationException) throw e
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
            wordsLearned < 500 -> "A1"
            wordsLearned < 1000 -> "A2"
            wordsLearned < 2000 -> "B1"
            wordsLearned < 4000 -> "B2"
            wordsLearned < 8000 -> "C1"
            else -> "C2"
        }
    }

    private fun calculateWeeklyActivity(sessions: List<StudySession>): List<DailyActivity> {
        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        // Set to Monday of this week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        // Handle Sunday if it's the start of the week in locale but we want Monday
        if (currentDayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, -6)
        }

        val mondayMillis = cal.timeInMillis
        val weeklyCounts = IntArray(7) { 0 }
        val labels = listOf("2", "3", "4", "5", "6", "7", "CN")
        val todayIdx = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY

        // Calculate counts and labels for Mon-Sun
        for (i in 0..6) {
            val dayStart = mondayMillis + i * TimeUnit.DAYS.toMillis(1)
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
            
            weeklyCounts[i] = sessions.filter { 
                it.timestamp in dayStart until dayEnd 
            }.sumOf { it.cardsStudied }
        }

        return labels.mapIndexed { index, label ->
            DailyActivity(
                day = label,
                wordsCount = weeklyCounts[index],
                isToday = index == todayIdx
            )
        }
    }

    private fun calculateTimeActivity(sessions: List<StudySession>): List<TimeActivity> {
        val cal = Calendar.getInstance()
        val morningCount = sessions.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 5..10
        }
        val noonCount = sessions.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 11..16
        }
        val eveningCount = sessions.count {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) in 17..22
        }
        val nightCount = sessions.count {
            cal.timeInMillis = it.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour >= 23 || hour < 5
        }

        return listOf(
            TimeActivity("Sáng", morningCount),
            TimeActivity("Trưa", noonCount),
            TimeActivity("Tối", eveningCount),
            TimeActivity("Đêm", nightCount)
        )
    }

    private fun sendReviewNotification(userId: String, count: Int) {
        viewModelScope.launch {
            try {
                // Kiểm tra xem hôm nay đã có thông báo ôn tập chưa để tránh trùng lặp trong DB
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis

                val existingNotifications = notificationRepo.getNotificationsByUserId(userId)
                val hasTodayNotification = existingNotifications.any { 
                    it.timestamp >= startOfToday && it.type == "review_reminder" 
                }

                if (!hasTodayNotification) {
                    val notification = com.example.minlish.data.model.Notification(
                        userId = userId,
                        title = "Ôn tập hàng ngày",
                        description = "Hôm nay bạn có $count từ vựng cần ôn tập. Hãy bắt đầu ngay nhé!",
                        type = "review_reminder",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    notificationRepo.addNotification(notification)
                }

                // Luôn hiển thị thông báo hệ thống/UI một lần mỗi phiên làm việc khi vào Dashboard
                _showReviewNotification.value = count
                hasShownNotificationThisSession = true
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error sending review notification", e)
            }
        }
    }

    fun clearNotificationEvent() {
        _showReviewNotification.value = null
    }
}
