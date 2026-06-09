package com.example.minlish.data.model

import com.google.firebase.firestore.PropertyName

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "info",
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false
)
