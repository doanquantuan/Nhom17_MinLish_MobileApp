package com.example.minlish.data.model

data class Vocabulary(
    val id: String = "",
    val setId: String = "",

    val word: String = "",
    val wordType: String = "",
    val pronunciation: String = "",
    val meaning: String = "",


    val description: String = "",
    val example: String = "",

    val collocation: String = "",

    val relatedWords: List<String> = emptyList(),

    val note: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val status: String = ""
)


