package com.example.minlish.model

import java.util.Date

data class VocabWord(
    val id: String = "",
    val word: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val meaning: String = "",
    val example: String = "",
    val collocation: String = "",
    val note: String = "",
    val deckId: String = "",
    
    // SM-2 parameters
    val easeFactor: Double = 2.5,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReview: Date = Date(),
    val lastReviewed: Date? = null,
    val status: WordStatus = WordStatus.NEW
)

enum class WordStatus {
    NEW, LEARNING, REVIEW, MASTERED
}

enum class Quality(val value: Int) {
    AGAIN(0), HARD(1), GOOD(2), EASY(3)
}
