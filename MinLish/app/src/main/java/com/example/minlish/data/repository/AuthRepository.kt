package com.example.minlish.data.repository

import com.example.minlish.data.firebase.AuthManager
import com.google.firebase.auth.FirebaseUser

class AuthRepository {
    fun register(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        AuthManager.register(email, pass, onSuccess, onError)
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        AuthManager.login(email, pass, onSuccess, onError)
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        AuthManager.loginWithGoogle(idToken, onSuccess, onError)
    }

    fun logout() {
        AuthManager.logout()
    }

    fun getCurrentUser(): FirebaseUser? = AuthManager.getCurrentUser()

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        AuthManager.sendPasswordResetEmail(email, onSuccess, onError)
    }
}
