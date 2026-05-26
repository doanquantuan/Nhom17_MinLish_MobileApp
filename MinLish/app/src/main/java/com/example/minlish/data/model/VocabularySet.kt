package com.example.minlish.data.model

data class VocabularySet(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    //val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updateAt: Long = System.currentTimeMillis()
)