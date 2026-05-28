package com.example.minlish.data.model

data class VocabularySet(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Tất cả",
    val progress: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updateAt: Long = System.currentTimeMillis()
)