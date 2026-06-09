package com.example.minlish.data.model

data class StudySession(
    val id: String = "",
    val userId: String = "",
    val setId: String = "",
    val cardsStudied: Int = 0,
    val correctAnswers: Int = 0,
    val durationMinutes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
