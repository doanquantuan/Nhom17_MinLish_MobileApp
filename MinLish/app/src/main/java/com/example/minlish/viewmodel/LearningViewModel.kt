package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.StudySession
import com.example.minlish.data.repository.StatsRepository
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.repository.VocabularySetRepository
import com.example.minlish.model.Quality
import com.example.minlish.model.VocabDeck
import com.example.minlish.model.VocabWord
import com.example.minlish.utils.Sm2Algorithm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LearningViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val statsRepo = StatsRepository(db)
    private val vocabRepo = VocabularyRepository(db)
    private val setRepo = VocabularySetRepository(db)

    private val _decks = MutableStateFlow<List<VocabDeck>>(emptyList())
    val decks: StateFlow<List<VocabDeck>> = _decks.asStateFlow()

    private val _currentSessionWords = MutableStateFlow<List<VocabWord>>(emptyList())
    val currentSessionWords: StateFlow<List<VocabWord>> = _currentSessionWords.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private var sessionStartTime: Long = 0

    init {
        loadRealDecks()
    }

    private fun loadRealDecks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val sets = setRepo.getSetsByUserId(userId)
                if (sets.isEmpty()) {
                    loadMockData()
                } else {
                    val vocabDecks = sets.map { set ->
                        VocabDeck(
                            id = set.id,
                            name = set.title,
                            totalWords = vocabRepo.getWordCountBySet(set.id),
                            wordsToReview = 0, // This could be calculated later with real SM-2 data
                            status = set.category,
                            colorHex = "#534AB7"
                        )
                    }
                    _decks.value = vocabDecks
                }
            } catch (e: Exception) {
                android.util.Log.e("LearningVM", "Error loading real decks", e)
                loadMockData()
            }
        }
    }

    private fun loadMockData() {
        _decks.value = listOf(
            VocabDeck("1", "IELTS Academic", 120, 14, "IELTS", "#5A4FCF"),
            VocabDeck("2", "Business Email", 56, 0, "Business", "#28C76F"),
            VocabDeck("3", "Travel English", 38, 3, "Travel", "#FF9F43")
        )
    }

    fun startSession(deckId: String, isReview: Boolean) {
        sessionStartTime = System.currentTimeMillis()
        _currentSessionWords.value = emptyList() // Reset to show loading
        _currentIndex.value = 0
        _isFinished.value = false
        
        viewModelScope.launch {
            try {
                // Get words from this deck
                val words = if (deckId == "all") {
                    vocabRepo.getWordsBySet("") 
                } else {
                    vocabRepo.getWordsBySet(deckId)
                }

                if (words.isEmpty()) {
                    // Fallback to mock for testing if no real words exist
                    val mockWords = listOf(
                        VocabWord(
                            id = "1",
                            word = "ambiguous",
                            phonetic = "/æmˈbɪɡjuəs/",
                            partOfSpeech = "adjective",
                            meaning = "mơ hồ, không rõ ràng",
                            example = "The contract terms were ambiguous and led to a long dispute between both parties.",
                            collocation = "ambiguous statement · ambiguous role",
                            deckId = deckId
                        ),
                        VocabWord(
                            id = "2",
                            word = "eloquent",
                            phonetic = "/ˈeləkwənt/",
                            partOfSpeech = "adjective",
                            meaning = "có tài hùng biện",
                            example = "She made an eloquent appeal for action.",
                            collocation = "eloquent speech · eloquent speaker",
                            deckId = deckId
                        )
                    )
                    _currentSessionWords.value = mockWords
                    _sessionStats.value = SessionStats(totalCards = mockWords.size)
                } else {
                    // Convert DB Vocabulary to Learning VocabWord
                    val sessionWords = words.map { 
                        VocabWord(
                            id = it.id,
                            word = it.word,
                            phonetic = it.pronunciation,
                            partOfSpeech = it.wordType,
                            meaning = it.meaning,
                            example = it.example,
                            collocation = it.collocation,
                            deckId = it.setId
                        )
                    }
                    _currentSessionWords.value = sessionWords
                    _sessionStats.value = SessionStats(totalCards = sessionWords.size)
                }
            } catch (e: Exception) {
                android.util.Log.e("LearningVM", "Error starting session, using fallback", e)
                // Fallback on error to prevent infinite loading
                val mockWords = listOf(
                    VocabWord(
                        id = "1",
                        word = "ambiguous",
                        phonetic = "/æmˈbɪɡjuəs/",
                        partOfSpeech = "adjective",
                        meaning = "mơ hồ, không rõ ràng",
                        example = "The contract terms were ambiguous and led to a long dispute between both parties.",
                        collocation = "ambiguous statement · ambiguous role",
                        deckId = deckId
                    ),
                    VocabWord(
                        id = "2",
                        word = "eloquent",
                        phonetic = "/ˈeləkwənt/",
                        partOfSpeech = "adjective",
                        meaning = "có tài hùng biện",
                        example = "She made an eloquent appeal for action.",
                        collocation = "eloquent speech · eloquent speaker",
                        deckId = deckId
                    )
                )
                _currentSessionWords.value = mockWords
                _sessionStats.value = SessionStats(totalCards = mockWords.size)
            }
        }
    }

    fun answerWord(quality: Quality) {
        val currentWords = _currentSessionWords.value
        val index = _currentIndex.value
        
        if (index < currentWords.size) {
            val updatedWord = Sm2Algorithm.calculateNextReview(currentWords[index], quality)
            
            val currentStats = _sessionStats.value
            val newStats = currentStats.copy(
                correctCount = if (quality.value >= 2) currentStats.correctCount + 1 else currentStats.correctCount,
                againCount = if (quality == Quality.AGAIN) currentStats.againCount + 1 else currentStats.againCount
            )
            _sessionStats.value = newStats

            if (index + 1 < currentWords.size) {
                _currentIndex.value = index + 1
            } else {
                saveSessionToFirestore()
                _isFinished.value = true
            }
        }
    }

    private fun saveSessionToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val stats = _sessionStats.value
        val duration = ((System.currentTimeMillis() - sessionStartTime) / 1000 / 60).toInt().coerceAtLeast(1)
        
        viewModelScope.launch {
            val session = StudySession(
                userId = userId,
                setId = "mock_set_id", // Should be real ID from startSession
                cardsStudied = stats.totalCards,
                correctAnswers = stats.correctCount,
                durationMinutes = duration
            )
            statsRepo.saveSession(session)
        }
    }

    data class SessionStats(
        val totalCards: Int = 0,
        val correctCount: Int = 0,
        val againCount: Int = 0,
        val timeMinutes: Int = 8
    ) {
        val accuracy: Int get() = if (totalCards > 0) (correctCount * 100 / totalCards) else 0
    }
}
