package com.example.minlish.model

data class VocabDeck(
    val id: String = "",
    val name: String = "",
    val totalWords: Int = 0,
    val wordsToReview: Int = 0,
    val wordsLearned: Int = 0,
    val status: String = "", // e.g., "IELTS", "Business", "Travel"
    val colorHex: String = "#FFFFFF"
)
