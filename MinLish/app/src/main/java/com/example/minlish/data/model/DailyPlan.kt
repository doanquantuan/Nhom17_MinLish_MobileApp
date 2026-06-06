package com.example.minlish.data.model

data class DailyPlan(
    val newWordsTarget: Int = 10,
    val reviewWordsCount: Int = 0,
    val newWordsLearned: Int = 0,
    val reviewWordsDone: Int = 0,
    val streak: Int = 0,
    val accuracy: Double = 0.0,
    val timeSpentMinutes: Int = 0
)
