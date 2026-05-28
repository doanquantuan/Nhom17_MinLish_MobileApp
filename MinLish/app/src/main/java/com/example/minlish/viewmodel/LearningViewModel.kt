package com.example.minlish.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minlish.model.Quality
import com.example.minlish.model.VocabDeck
import com.example.minlish.model.VocabWord
import com.example.minlish.utils.Sm2Algorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LearningViewModel : ViewModel() {

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

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _decks.value = listOf(
            VocabDeck("1", "IELTS Academic", 120, 14, "IELTS", "#5A4FCF"),
            VocabDeck("2", "Business Email", 56, 0, "Business", "#28C76F"),
            VocabDeck("3", "Travel English", 38, 3, "Travel", "#FF9F43")
        )
    }

    fun startSession(deckId: String, isReview: Boolean) {
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
        _currentIndex.value = 0
        _isFinished.value = false
        _sessionStats.value = SessionStats(totalCards = mockWords.size)
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
                _isFinished.value = true
            }
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
