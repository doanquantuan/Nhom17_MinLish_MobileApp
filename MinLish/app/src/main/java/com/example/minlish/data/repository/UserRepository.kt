package com.example.minlish.data.repository

<<<<<<< HEAD
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    // Lưu thông tin khảo sát onboarding
    fun saveOnboardingData(
        userId: String,
        goal: String,
        level: String,
        wordsPerDay: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userProfile = hashMapOf(
            "goal" to goal,
            "level" to level,
            "wordsPerDay" to wordsPerDay,
            "onboardingCompleted" to true
        )
        db.collection("users").document(userId)
            .set(userProfile, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Lỗi lưu dữ liệu") }
    }

    // Lấy thông tin User hiển thị ở Profile
    fun getUserProfile(userId: String, onSuccess: (com.google.firebase.firestore.DocumentSnapshot) -> Unit) {
        db.collection("users").document(userId).get().addOnSuccessListener { onSuccess(it) }
    }

    // Cập nhật tên User
    fun updateUserName(userId: String, newName: String, onSuccess: () -> Unit) {
        db.collection("users").document(userId)
            .set(mapOf("name" to newName), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
    }
}
=======
>>>>>>> feature/learning
