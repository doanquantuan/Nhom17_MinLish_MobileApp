package com.example.minlish.firebase
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
                // 1. Đăng ký xong thì gửi mail xác thực ngay
                auth.currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess() // Gửi mail thành công thì mới báo Đăng ký thành công
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
}
