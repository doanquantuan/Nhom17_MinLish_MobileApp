package com.example.minlish.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val goal: String = "",
    val level: String = "",
    val wordsPerDay: Int = 0,
    val onboardingCompleted: Boolean = false
)
