package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.StudySession
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.data.repository.StatsRepository
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.repository.VocabularySetRepository
import com.example.minlish.model.Quality
import com.example.minlish.model.VocabDeck
import com.example.minlish.model.VocabWord
import com.example.minlish.utils.Sm2Algorithm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.minlish.data.model.Notification
import com.example.minlish.data.repository.NotificationRepository
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LearningViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val statsRepo = StatsRepository(db)
    private val vocabRepo = VocabularyRepository(db)
    private val setRepo = VocabularySetRepository(db)
    private val notificationRepo = NotificationRepository(db)

    private val _decks = MutableStateFlow<List<VocabDeck>>(emptyList())
    val decks: StateFlow<List<VocabDeck>> = _decks.asStateFlow()

    private val _totalWordsToReview = MutableStateFlow(0)
    val totalWordsToReview: StateFlow<Int> = _totalWordsToReview.asStateFlow()

    private val _filterMode = MutableStateFlow(DeckFilterMode.ALL)
    val filterMode: StateFlow<DeckFilterMode> = _filterMode.asStateFlow()

    val filteredDecks = combine(_decks, _filterMode) { decks, mode ->
        when (mode) {
            DeckFilterMode.ALL -> decks
            DeckFilterMode.LEARN_NEW -> decks.filter { deck ->
                deck.totalWords > deck.wordsToReview
            }
            DeckFilterMode.REVIEW -> decks.filter { deck ->
                deck.wordsToReview > 0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resetFinishedStatus() {
        _isFinished.value = false
    }

    fun setFilterMode(mode: DeckFilterMode) {
        _filterMode.value = mode
    }

    private val _currentSessionWords = MutableStateFlow<List<VocabWord>>(emptyList())
    val currentSessionWords: StateFlow<List<VocabWord>> = _currentSessionWords.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _isLoadingSession = MutableStateFlow(false)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private var sessionStartTime: Long = 0
    private var currentDeckId: String = ""
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    init {
        loadRealDecks()
        loadCurrentStreak()
    }

    private fun loadCurrentStreak() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val sessions = statsRepo.getSessionsByUserId(userId)
                _currentStreak.value = calculateStreak(sessions)
            } catch (e: Exception) {
                android.util.Log.e("LearningVM", "Error loading streak", e)
            }
        }
    }

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        
        val uniqueDays = sessions.map { 
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val today = java.util.Calendar.getInstance()
        today.set(java.util.Calendar.HOUR_OF_DAY, 0)
        today.set(java.util.Calendar.MINUTE, 0)
        today.set(java.util.Calendar.SECOND, 0)
        today.set(java.util.Calendar.MILLISECOND, 0)
        val todayTime = today.timeInMillis

        val yesterdayTime = todayTime - 86400000L

        if (uniqueDays[0] < yesterdayTime) return 0

        var streak = 0
        var expectedTime = uniqueDays[0]

        for (day in uniqueDays) {
            if (day == expectedTime) {
                streak++
                expectedTime -= 86400000L
            } else {
                break
            }
        }
        return streak
    }

    fun loadRealDecks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val sets = setRepo.getSetsByUserId(userId)
                val vocabDecks = sets.map { set ->
                    val allSetWords = vocabRepo.getWordsBySet(set.id)
                    val total = allSetWords.size
                    val toReview = allSetWords.count { it.repetitions > 0 } // Any word studied is in review pool
                    val learned = allSetWords.count { it.status == "Thuộc" }

                    VocabDeck(
                        id = set.id,
                        name = set.title,
                        totalWords = total,
                        wordsToReview = toReview,
                        wordsLearned = learned,
                        status = set.category,
                        colorHex = "#534AB7"
                    )
                }
                _decks.value = vocabDecks
                val now = System.currentTimeMillis()
                _totalWordsToReview.value = sets.sumOf { set ->
                    vocabRepo.getWordsBySet(set.id).count { w -> 
                        w.repetitions > 0 && w.nextReview <= now 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LearningVM", "Error loading real decks", e)
                _decks.value = emptyList()
            }
        }
    }

    fun startSession(deckId: String, isReview: Boolean) {
        currentDeckId = deckId
        sessionStartTime = System.currentTimeMillis()
        _currentSessionWords.value = emptyList() 
        _currentIndex.value = 0
        _isFinished.value = false
        _isLoadingSession.value = true
        
        viewModelScope.launch {
            try {
                // Check if it's mock data
                val allWords = if (deckId == "all") {
                    val userId = auth.currentUser?.uid ?: ""
                    if (userId.isEmpty()) emptyList<Vocabulary>()
                    else {
                        val sets = setRepo.getSetsByUserId(userId)
                        val wordsList = mutableListOf<Vocabulary>()
                        sets.forEach { set ->
                            wordsList.addAll(vocabRepo.getWordsBySet(set.id))
                        }
                        wordsList
                    }
                } else {
                    vocabRepo.getWordsBySet(deckId)
                }

                val now = System.currentTimeMillis()
                val filteredWords = if (isReview) {
                    if (deckId == "all") {
                        // Global "Ôn ngay": only words that are actually due.
                        allWords.filter { it.repetitions > 0 && it.nextReview <= now }
                            .sortedBy { it.nextReview } // Oldest reviews first
                    } else {
                        // Specific Deck "Ôn tập": allow early review of all studied words
                        allWords.filter { it.repetitions > 0 }
                            .sortedBy { it.nextReview } // Show due/overdue words first, then others
                    }
                } else {
                    // Learn New mode: words that are NEW
                    allWords.filter { it.repetitions == 0 }
                }

                if (filteredWords.isEmpty()) {
                    _currentSessionWords.value = emptyList()
                    _sessionStats.value = SessionStats(totalCards = 0)
                } else {
                    // Convert DB Vocabulary to Learning VocabWord
                    val sessionWords = filteredWords.map { 
                        VocabWord(
                            id = it.id,
                            word = it.word,
                            phonetic = it.pronunciation,
                            partOfSpeech = it.wordType,
                            meaning = it.meaning,
                            description = it.description,
                            example = it.example,
                            collocation = it.collocation,
                            note = it.note,
                            deckId = it.setId,
                            easeFactor = it.easeFactor,
                            interval = it.interval,
                            repetitions = it.repetitions,
                            nextReview = Date(it.nextReview),
                            lastReviewed = it.lastReviewed?.let { lr -> Date(lr) },
                            status = when(it.status) {
                                "Ôn lại" -> com.example.minlish.model.WordStatus.REVIEW
                                "Thuộc" -> com.example.minlish.model.WordStatus.MASTERED
                                else -> com.example.minlish.model.WordStatus.NEW
                            }
                        )
                    }
                    _currentSessionWords.value = sessionWords
                    _sessionStats.value = SessionStats(totalCards = sessionWords.size)
                }
            } catch (e: Exception) {
                android.util.Log.e("LearningVM", "Error starting session", e)
                _currentSessionWords.value = emptyList()
            } finally {
                _isLoadingSession.value = false
            }
        }
    }

    fun answerWord(quality: Quality) {
        val currentWords = _currentSessionWords.value.toMutableList()
        val index = _currentIndex.value
        
        if (index < currentWords.size) {
            val word = currentWords[index]
            val updatedWord = Sm2Algorithm.calculateNextReview(word, quality)
            currentWords[index] = updatedWord
            _currentSessionWords.value = currentWords
            
            // Record result for summary
            val speedStatus = when (quality) {
                Quality.EASY -> "Dễ"
                Quality.GOOD -> "Bình thường"
                Quality.HARD -> "Khó"
                Quality.AGAIN -> "Quên"
            }
            val result = WordResult(word.word, speedStatus)
            
            // Save to Firestore using a background job that we can track
            viewModelScope.launch {
                try {
                    val originalVocab = vocabRepo.getWordById(updatedWord.id)
                    if (originalVocab != null) {
                        val newStatus = when {
                            updatedWord.status == com.example.minlish.model.WordStatus.MASTERED -> "Thuộc"
                            updatedWord.status == com.example.minlish.model.WordStatus.NEW -> "Mới"
                            else -> "Ôn lại"
                        }
                        
                        val updatedVocab = originalVocab.copy(
                            status = newStatus,
                            easeFactor = updatedWord.easeFactor,
                            interval = updatedWord.interval,
                            repetitions = updatedWord.repetitions,
                            nextReview = updatedWord.nextReview.time,
                            lastReviewed = updatedWord.lastReviewed?.time
                        )
                        
                        android.util.Log.d("LearningVM", "SAVING WORD: ${updatedVocab.word} | STATUS: ${updatedVocab.status} | REPS: ${updatedVocab.repetitions}")
                        vocabRepo.updateWord(updatedVocab)
                        
                        // Critical: Also update the local decks immediately so the UI doesn't have to wait for refreshData
                        loadRealDecks()
                        
                        android.util.Log.d("LearningVM", "SAVE SUCCESS: ${updatedVocab.word}")

                        // Finalize session if this was the last word
                        if (index + 1 == currentWords.size) {
                            val durationMs = System.currentTimeMillis() - sessionStartTime
                            val durationSeconds = (durationMs / 1000).toInt()
                            _sessionStats.value = _sessionStats.value.copy(totalTimeSeconds = durationSeconds)
                            
                            saveSessionToFirestore()
                            loadCurrentStreak() // Refresh streak after saving
                            _isFinished.value = true
                            loadRealDecks() // Explicitly refresh decks locally
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LearningVM", "CRITICAL ERROR SAVING WORD", e)
                }
            }

            val currentStats = _sessionStats.value
            val newStats = currentStats.copy(
                correctCount = if (quality.value >= 2) currentStats.correctCount + 1 else currentStats.correctCount,
                againCount = if (quality.value < 2) currentStats.againCount + 1 else currentStats.againCount,
                wordResults = currentStats.wordResults + result
            )
            _sessionStats.value = newStats

            if (index + 1 < currentWords.size) {
                _currentIndex.value = index + 1
            }
        }
    }

    private suspend fun saveSessionToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val stats = _sessionStats.value
        
        try {
            val session = StudySession(
                userId = userId,
                setId = currentDeckId, 
                cardsStudied = stats.totalCards,
                correctAnswers = stats.correctCount,
                durationMinutes = stats.totalTimeSeconds / 60,
                timestamp = System.currentTimeMillis()
            )
            statsRepo.saveSession(session)

            // Update set progress
            if (currentDeckId != "all") {
                val total = vocabRepo.getWordCountBySet(currentDeckId)
                val learned = vocabRepo.getLearnedCountBySet(currentDeckId)
                val progress = if (total > 0) (learned * 100 / total) else 0
                
                val currentSet = setRepo.getSetById(currentDeckId)
                if (currentSet != null) {
                    setRepo.updateSet(currentSet.copy(progress = progress))
                }
            }

            // Tự động tạo thông báo chúc mừng khi hoàn thành bài học
            val notification = Notification(
                userId = userId,
                title = "Hoàn thành bài học!",
                description = "Bạn đã hoàn thành ${stats.totalCards} từ vựng. Tuyệt vời!",
                type = "streak",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            notificationRepo.addNotification(notification)

            loadRealDecks()
        } catch (e: Exception) {
            android.util.Log.e("LearningVM", "Error saving session and updating progress", e)
        }
    }

    data class SessionStats(
        val totalCards: Int = 0,
        val correctCount: Int = 0,
        val againCount: Int = 0,
        val totalTimeSeconds: Int = 0,
        val wordResults: List<WordResult> = emptyList()
    ) {
        val accuracy: Int get() = if (totalCards > 0) (correctCount * 100 / totalCards) else 0
        val timeDisplay: String get() {
            val minutes = totalTimeSeconds / 60
            val seconds = totalTimeSeconds % 60
            return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        }
    }
}

data class WordResult(
    val word: String,
    val speedStatus: String
)

enum class DeckFilterMode {
    ALL, LEARN_NEW, REVIEW
}
