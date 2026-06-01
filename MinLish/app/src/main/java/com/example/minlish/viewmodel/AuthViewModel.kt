package com.example.minlish.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.minlish.data.repository.AuthRepository
import com.example.minlish.data.repository.UserRepository
import com.example.minlish.utils.ReminderManager

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
    var showVerificationDialog by mutableStateOf(false)

    // Trạng thái dữ liệu màn hình Profile
    var userName by mutableStateOf("Tên của bạn")
    var userEmail by mutableStateOf("")
    var userLevel by mutableStateOf("Đang tải...")
    var userWordsPerDay by mutableIntStateOf(0)
    var userReminderTime by mutableStateOf("20:00")
    var isEditingName by mutableStateOf(false)

    init {
        val currentUser = authRepo.getCurrentUser()
        userEmail = currentUser?.email ?: ""
        userName = currentUser?.displayName ?: "Người dùng"
        loadUserProfile()
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
                    showVerificationDialog = true
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
    fun loadUserProfile(context: Context? = null) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        userEmail = authRepo.getCurrentUser()?.email ?: ""
        userRepo.getUserProfile(uid) { document ->
            if (document.exists()) {
                document.getString("name")?.let { userName = it }
                document.getString("level")?.let {
                    userLevel = it.split(" ").firstOrNull() ?: it
                }
                document.getLong("wordsPerDay")?.let { userWordsPerDay = it.toInt() }
                document.getString("reminderTime")?.let { 
                    userReminderTime = it 
                    // Tự động lập lịch khi tải profile xong
                    context?.let { ctx ->
                        ReminderManager.scheduleReminder(ctx, userReminderTime)
                    }
                }
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

    // LOGIC ĐỔI GIỜ NHẮC NHỞ
    fun updateReminderTime(context: Context, newTime: String) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        userRepo.updateReminderTime(uid, newTime) {
            userReminderTime = newTime
            // Cập nhật lại lịch nhắc nhở
            ReminderManager.scheduleReminder(context, newTime)
        }
    }

    fun logout(onComplete: () -> Unit) {
        authRepo.logout()
        onComplete()
    }

    fun resendVerificationEmail() {
        isLoading = true
        authRepo.sendVerificationEmail(
            onSuccess = {
                isLoading = false
                toastMessage = "Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư."
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }

    fun dismissVerificationDialog() {
        showVerificationDialog = false
        authRepo.logout()
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

    fun resetPassword(email: String, onComplete: () -> Unit) {
        if (email.isBlank()) {
            toastMessage = "Vui lòng nhập email"
            return
        }
        isLoading = true
        authRepo.sendPasswordResetEmail(email,
            onSuccess = {
                isLoading = false
                toastMessage = "Email khôi phục mật khẩu đã được gửi! Vui lòng kiểm tra hộp thư."
                onComplete() // Đóng hộp thoại khi gửi thành công
            },
            onError = {
                isLoading = false
                toastMessage = it
            }
        )
    }
}
