package com.example.minlish.data.model

data class Vocabulary(
    val id: String = "",
    val setId: String = "",

    val word: String = "",
    val wordType: String = "",
    val pronunciation: String = "",
    val meaning: String = "",

    val example: String = "",

    val collocation: String = "",

    val relatedWords: List<String> = emptyList(),

    val note: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val status: String = "Mới",
    
    // SM-2 parameters for persistence
    val easeFactor: Double = 2.5,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReview: Long = System.currentTimeMillis(),
    val lastReviewed: Long? = null,
    val firstReviewedAt: Long? = null
)

enum class WordStatus {
    NEW, LEARNING, REVIEW, MASTERED
}

enum class Quality(val value: Int) {
    AGAIN(0), HARD(1), GOOD(2), EASY(3)
}


