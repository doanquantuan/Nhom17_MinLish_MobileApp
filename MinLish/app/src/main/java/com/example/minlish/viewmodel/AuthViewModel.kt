package com.example.minlish.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.minlish.data.repository.AuthRepository
import com.example.minlish.data.repository.UserRepository

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()

    // Trạng thái chung
    var isLoading by mutableStateOf(false)
    var toastMessage by mutableStateOf<String?>(null)

    // Trạng thái điều hướng luồng
    var navigateToDashboard by mutableStateOf(false)
    var navigateToOnboarding by mutableStateOf(false)
    var registerSuccess by mutableStateOf(false)

    // Trạng thái dữ liệu màn hình Profile
    var userName by mutableStateOf("Tên của bạn")
    var userEmail by mutableStateOf("")
    var userLevel by mutableStateOf("Đang tải...")
    var userWordsPerDay by mutableIntStateOf(0)
    var isEditingName by mutableStateOf(false)

    init {
        userEmail = authRepo.getCurrentUser()?.email ?: ""
    }

    // LOGIC ĐĂNG NHẬP EMAIL
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            toastMessage = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        isLoading = true
        authRepo.login(email, pass,
            onSuccess = {
                val user = authRepo.getCurrentUser()
                if (user != null && user.isEmailVerified) {
                    checkUserOnboarding(user.uid)
                } else {
                    isLoading = false
                    toastMessage = "Tài khoản chưa xác thực! Vui lòng kiểm tra Email."
                    authRepo.logout()
                }
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }

    // LOGIC ĐĂNG NHẬP GOOGLE
    fun loginWithGoogle(idToken: String) {
        isLoading = true
        authRepo.loginWithGoogle(idToken,
            onSuccess = {
                val user = authRepo.getCurrentUser()
                if (user != null) checkUserOnboarding(user.uid)
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }

    // LOGIC ĐĂNG KÝ
    fun register(email: String, pass: String, confirmPass: String) {
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            toastMessage = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        if (pass != confirmPass) {
            toastMessage = "Mật khẩu không khớp"
            return
        }
        isLoading = true
        authRepo.register(email, pass,
            onSuccess = {
                isLoading = false
                authRepo.logout()
                registerSuccess = true
                toastMessage = "Đăng ký thành công! Vui lòng kiểm tra email để xác thực."
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }

    // LOGIC LƯU KHẢO SÁT ONBOARDING
    fun saveOnboarding(goal: String, level: String, wordsPerDay: Int) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        isLoading = true
        userRepo.saveOnboardingData(uid, goal, level, wordsPerDay,
            onSuccess = {
                isLoading = false
                navigateToDashboard = true
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }

    // LOGIC TẢI DỮ LIỆU PROFILE
    fun loadUserProfile() {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        userEmail = authRepo.getCurrentUser()?.email ?: ""
        userRepo.getUserProfile(uid) { document ->
            if (document.exists()) {
                document.getString("name")?.let { userName = it }
                document.getString("level")?.let {
                    userLevel = it.split(" ").firstOrNull() ?: it
                }
                document.getLong("wordsPerDay")?.let { userWordsPerDay = it.toInt() }
            }
        }
    }

    // LOGIC ĐỔI TÊN Ở PROFILE
    fun updateName(newName: String) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        userRepo.updateUserName(uid, newName) {
            isEditingName = false
            userName = newName
        }
    }

    fun logout(onComplete: () -> Unit) {
        authRepo.logout()
        onComplete()
    }

    // Kiểm tra phân luồng người dùng cũ / mới
    private fun checkUserOnboarding(userId: String) {
        userRepo.getUserProfile(userId) { document ->
            isLoading = false
            if (document.exists() && document.getBoolean("onboardingCompleted") == true) {
                navigateToDashboard = true
            } else {
                navigateToOnboarding = true
            }
        }
    }
}