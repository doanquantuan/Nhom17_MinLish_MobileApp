package com.example.minlish.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlish.data.model.Vocabulary
import com.example.minlish.data.repository.VocabularyRepository
import com.example.minlish.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class GameViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val db = FirebaseFirestore.getInstance()
    private val vocabRepo = VocabularyRepository(db)

    // TTS
    private var tts: TextToSpeech? = TextToSpeech(application, this)
    private val _isTtsReady = MutableStateFlow(false)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            _isTtsReady.value = true
        }
    }

    fun speak(text: String) {
        if (_isTtsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    // Quiz State
    private val _quizSession = MutableStateFlow<QuizSession?>(null)
    val quizSession = _quizSession.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    // Matching State
    private val _matchingSession = MutableStateFlow<MatchingSession?>(null)
    val matchingSession = _matchingSession.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Sinh bộ câu hỏi trắc nghiệm
     */
    fun startQuiz(setId: String, totalQuestions: Int = 10, allowRepeat: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Lọc chính xác theo trạng thái: Đã thuộc hoặc Đang ôn tập (Ôn lại)
                val allWordsInSet = vocabRepo.getWordsBySet(setId)
                val words = allWordsInSet.filter { 
                    it.status == "Thuộc" || it.status == "Thuoc" || 
                    it.status == "Ôn lại" || it.status == "On lai" 
                }
                
                if (words.size < 2) {
                    _isLoading.value = false
                    // Có thể thêm thông báo cho người dùng ở đây nếu cần
                    return@launch
                }

                val questions = mutableListOf<QuizQuestion>()
                val distractorUsageCount = mutableMapOf<String, Int>()
                var lastWordId = ""

                for (i in 0 until totalQuestions) {
                    // Chọn từ mục tiêu (target word)
                    val availableWords = if (!allowRepeat) {
                        words.filter { w -> !questions.any { q -> q.wordId == w.id } }
                    } else {
                        words.filter { it.id != lastWordId }
                    }

                    if (availableWords.isEmpty()) break

                    val targetWord = availableWords.random()
                    lastWordId = targetWord.id

                    val direction = if (Random().nextBoolean()) QuizDirection.VI_TO_EN else QuizDirection.EN_TO_VI
                    
                    // Tạo distractors
                    val distractors = generateDistractors(targetWord, words, distractorUsageCount, direction)
                    
                    val question = QuizQuestion(
                        wordId = targetWord.id,
                        questionText = if (direction == QuizDirection.VI_TO_EN) targetWord.meaning else targetWord.word,
                        correctAnswer = if (direction == QuizDirection.VI_TO_EN) targetWord.word else targetWord.meaning,
                        choices = (distractors + (if (direction == QuizDirection.VI_TO_EN) targetWord.word else targetWord.meaning)).shuffled(),
                        explanation = "${targetWord.word}: ${targetWord.meaning}",
                        direction = direction
                    )
                    questions.add(question)
                }

                _quizSession.value = QuizSession(questions)
                _currentQuestionIndex.value = 0
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Error starting quiz", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateDistractors(
        target: Vocabulary,
        allWords: List<Vocabulary>,
        usageCount: MutableMap<String, Int>,
        direction: QuizDirection
    ): List<String> {
        val distractors = mutableListOf<String>()
        val otherWords = allWords.filter { it.id != target.id }.shuffled()
        
        for (word in otherWords) {
            val count = usageCount.getOrDefault(word.id, 0)
            if (count < 3) {
                val distractorText = if (direction == QuizDirection.VI_TO_EN) word.word else word.meaning
                distractors.add(distractorText)
                usageCount[word.id] = count + 1
            }
            if (distractors.size == 3) break
        }

        // Dự phòng nếu deck quá ít từ
        while (distractors.size < 3) {
            distractors.add("Dữ liệu mẫu ${distractors.size + 1}")
        }

        return distractors
    }

    /**
     * Sinh bộ thẻ Matching
     */
    fun startMatching(setId: String, totalPairs: Int = 6) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Lọc chính xác theo trạng thái: Đã thuộc hoặc Đang ôn tập (Ôn lại)
                val allWordsInSet = vocabRepo.getWordsBySet(setId)
                val learnedWords = allWordsInSet.filter { 
                    it.status == "Thuộc" || it.status == "Thuoc" || 
                    it.status == "Ôn lại" || it.status == "On lai" 
                }
                
                val words = learnedWords.shuffled().take(totalPairs)
                val allCards = mutableListOf<MatchingCard>()
                
                words.forEach { word ->
                    allCards.add(MatchingCard(id = UUID.randomUUID().toString(), pairId = word.id, text = word.word, type = CardType.EN))
                    allCards.add(MatchingCard(id = UUID.randomUUID().toString(), pairId = word.id, text = word.meaning, type = CardType.VI))
                }

                val session = MatchingSession(
                    allCards = allCards,
                    startTime = System.currentTimeMillis()
                )
                _matchingSession.value = session
                setupNextRound()
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Error starting matching", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAnswerSelected(answer: String) {
        val session = _quizSession.value ?: return
        val currentQ = session.questions[session.currentIndex]
        
        val newScore = if (answer == currentQ.correctAnswer) session.score + 1 else session.score
        val nextIndex = session.currentIndex + 1

        // Ghi lại câu trả lời của người dùng
        session.userAnswers.add(if (answer.isEmpty()) null else answer)

        _currentQuestionIndex.value = nextIndex
        _quizSession.value = session.copy(
            currentIndex = nextIndex,
            score = newScore
        )
    }

    private fun setupNextRound() {
        val session = _matchingSession.value ?: return
        val remainingCards = session.allCards.filter { !it.isMatched }
        
        if (remainingCards.isEmpty()) {
            _matchingSession.value = session.copy(isFinished = true)
            return
        }

        // Lấy tối đa 6 cặp (12 thẻ) cho mỗi màn hình (3x4)
        val roundPairsCount = minOf(6, remainingCards.size / 2)
        val roundPairIds = remainingCards.map { it.pairId }.distinct().take(roundPairsCount)
        val roundCards = remainingCards.filter { it.pairId in roundPairIds }.shuffled()

        _matchingSession.value = session.copy(currentRoundCards = roundCards)
    }

    private var firstSelectedCard: MatchingCard? = null

    fun onCardClicked(card: MatchingCard) {
        val session = _matchingSession.value ?: return
        if (card.isMatched || card.isSelected || session.isFinished) return

        val updatedRoundCards = session.currentRoundCards.map { 
            if (it.id == card.id) it.copy(isSelected = true, isError = false) 
            else if (it.isError) it.copy(isError = false)
            else it 
        }
        _matchingSession.value = session.copy(currentRoundCards = updatedRoundCards)

        if (firstSelectedCard == null) {
            firstSelectedCard = updatedRoundCards.find { it.id == card.id }
        } else {
            val secondCard = updatedRoundCards.find { it.id == card.id }!!
            if (firstSelectedCard!!.pairId == secondCard.pairId && firstSelectedCard!!.type != secondCard.type) {
                // Match!
                val matchedRoundCards = updatedRoundCards.map {
                    if (it.id == firstSelectedCard!!.id || it.id == secondCard.id) {
                        it.copy(isSelected = false, isMatched = true)
                    } else it
                }
                
                // Cập nhật trạng thái matched trong danh sách tổng allCards
                val updatedAllCards = session.allCards.map { cardInAll ->
                    if (cardInAll.pairId == firstSelectedCard!!.pairId) {
                        cardInAll.copy(isMatched = true)
                    } else cardInAll
                }

                val newSession = session.copy(
                    allCards = updatedAllCards,
                    currentRoundCards = matchedRoundCards,
                    totalMatchesFound = session.totalMatchesFound + 1
                )
                _matchingSession.value = newSession

                // Kiểm tra nếu vòng này đã xong
                if (matchedRoundCards.all { it.isMatched }) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500) // Đợi chút cho user thấy kết quả xanh
                        setupNextRound()
                    }
                }
            } else {
                // Mismatch
                val errorRoundCards = updatedRoundCards.map {
                    if (it.id == firstSelectedCard!!.id || it.id == secondCard.id) {
                        it.copy(isSelected = false, isError = true)
                    } else it
                }
                _matchingSession.value = session.copy(
                    currentRoundCards = errorRoundCards,
                    totalErrors = session.totalErrors + 1
                )
            }
            firstSelectedCard = null
        }
    }
}
