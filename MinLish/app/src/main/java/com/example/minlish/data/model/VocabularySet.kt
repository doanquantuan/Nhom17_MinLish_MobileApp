package com.example.minlish.data.model

import com.google.firebase.firestore.Exclude

data class VocabularySet(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Tất cả",
    val progress: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updateAt: Long = System.currentTimeMillis(),

    // Learning stats (Calculated at runtime, excluded from Firestore if needed)
    @get:Exclude val totalWords: Int = 0,
    @get:Exclude val wordsToReview: Int = 0,
    @get:Exclude val wordsLearned: Int = 0,
    @get:Exclude val colorHex: String = "#534AB7"
)
