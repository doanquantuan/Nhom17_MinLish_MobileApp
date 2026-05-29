package com.example.minlish.data.firebase

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth

object AuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("Không thể gửi email xác thực: ${task.exception?.message}")
                        }
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Đăng ký thất bại")
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Đăng nhập thất bại")
            }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Lỗi đăng nhập Google")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gửi email khôi phục thất bại") }
    }
}
