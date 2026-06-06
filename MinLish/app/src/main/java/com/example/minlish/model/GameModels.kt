package com.example.minlish.model

import com.example.minlish.data.model.Vocabulary

data class QuizQuestion(
    val wordId: String,
    val questionText: String,
    val correctAnswer: String,
    val choices: List<String>,
    val explanation: String,
    val direction: QuizDirection
)

enum class QuizDirection {
    VI_TO_EN, EN_TO_VI
}

data class MatchingCard(
    val id: String,
    val pairId: String,
    val text: String,
    val type: CardType,
    var isSelected: Boolean = false,
    var isMatched: Boolean = false,
    var isError: Boolean = false
)

enum class CardType {
    EN, VI
}

data class QuizSession(
    val questions: List<QuizQuestion>,
    var currentIndex: Int = 0,
    var score: Int = 0,
    val userAnswers: MutableList<String?> = mutableListOf()
)

data class MatchingSession(
    val allCards: List<MatchingCard>,
    var currentRoundCards: List<MatchingCard> = emptyList(),
    var startTime: Long = 0,
    var totalMatchesFound: Int = 0,
    var totalErrors: Int = 0,
    var isFinished: Boolean = false
)
