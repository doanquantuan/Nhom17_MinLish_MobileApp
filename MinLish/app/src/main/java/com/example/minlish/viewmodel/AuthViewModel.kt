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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

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
    var isReminderEnabled by mutableStateOf(true)
    var isEditingName by mutableStateOf(false)

    init {
        val currentUser = authRepo.getCurrentUser()
        userEmail = currentUser?.email ?: ""
        userName = currentUser?.displayName ?: "Người dùng"
        loadUserProfile()
    }

    private fun translateAuthError(errorMsg: String): String {
        val lowerCaseError = errorMsg.lowercase()
        return when {
            lowerCaseError.contains("invalid_login_credentials") ||
                    lowerCaseError.contains("invalid credential") ||
                    lowerCaseError.contains("credential is incorrect") ||
                    lowerCaseError.contains("wrong password") ->
                "Email hoặc mật khẩu không chính xác!"
            lowerCaseError.contains("badly formatted") || lowerCaseError.contains("invalid_email") ->
                "Định dạng email không hợp lệ!"
            lowerCaseError.contains("already in use") || lowerCaseError.contains("email_already_in_use") ->
                "Email này đã được đăng ký cho một tài khoản khác!"
            lowerCaseError.contains("at least 6") || lowerCaseError.contains("password is invalid") ->
                "Mật khẩu quá yếu (cần ít nhất 6 ký tự)."
            lowerCaseError.contains("user not found") || lowerCaseError.contains("user_not_found") ->
                "Tài khoản không tồn tại!"
            lowerCaseError.contains("network error") || lowerCaseError.contains("network_error") ->
                "Lỗi mạng! Vui lòng kiểm tra kết nối Internet."
            lowerCaseError.contains("too many requests") || lowerCaseError.contains("too_many_requests") ->
                "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau!"
            lowerCaseError.contains("blocked") || lowerCaseError.contains("disabled") ->
                "Tài khoản này đã bị khóa."
            lowerCaseError.contains("requires recent authentication") ->
                "Vì lý do bảo mật, vui lòng Đăng xuất rồi Đăng nhập lại để thực hiện!"
            else -> "Thao tác thất bại: $errorMsg"
        }
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
                    toastMessage = "Đăng nhập thành công!"
                    checkUserOnboarding(user.uid)
                } else {
                    isLoading = false
                    showVerificationDialog = true
                }
            },
            onError = {
                isLoading = false
                toastMessage = translateAuthError(it)
            }
        )
    }

    // LOGIC ĐĂNG NHẬP GOOGLE
    fun loginWithGoogle(idToken: String) {
        isLoading = true
        authRepo.loginWithGoogle(idToken,
            onSuccess = {
                val user = authRepo.getCurrentUser()
                if (user != null)
                {
                    toastMessage = "Đăng nhập thành công!"
                    checkUserOnboarding(user.uid)
                }
            },
            onError = {
                isLoading = false
                toastMessage = translateAuthError(it)
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
                toastMessage = translateAuthError(it)
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
                toastMessage = translateAuthError(it)
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
                document.getBoolean("isReminderEnabled")?.let { isReminderEnabled = it }
                document.getString("reminderTime")?.let { 
                    userReminderTime = it 
                    // Tự động lập lịch khi tải profile xong nếu đang bật nhắc nhở
                    context?.let { ctx ->
                        if (isReminderEnabled) {
                            ReminderManager.scheduleReminder(ctx, userReminderTime)
                        } else {
                            ReminderManager.cancelReminder(ctx)
                        }
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
            // Cập nhật lại lịch nhắc nhở nếu đang bật
            if (isReminderEnabled) {
                ReminderManager.scheduleReminder(context, newTime)
            }
        }
    }

    // LOGIC BẬT/TẮT NHẮC NHỞ
    fun updateReminderStatus(context: Context, isEnabled: Boolean) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        userRepo.updateReminderStatus(uid, isEnabled) {
            isReminderEnabled = isEnabled
            if (isEnabled) {
                ReminderManager.scheduleReminder(context, userReminderTime)
            } else {
                ReminderManager.cancelReminder(context)
            }
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
                toastMessage = translateAuthError(it)
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
                toastMessage = translateAuthError(it)
            }
        )
    }

    fun updateWordsPerDay(newWords: Int) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("wordsPerDay", newWords)
            .addOnSuccessListener {
                userWordsPerDay = newWords // Cập nhật lại giao diện ngay lập tức
            }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = authRepo.getCurrentUser()
        val uid = user?.uid
        if (user == null || uid == null) {
            onError("Không tìm thấy tài khoản để xóa.")
            return
        }

        // --- BƯỚC CHẶN THÔNG MINH: KIỂM TRA THỜI GIAN ĐĂNG NHẬP GẦN NHẤT ---
        // Firebase Auth lưu lại khoảnh khắc bạn đăng nhập bằng biến lastSignInTimestamp
        val lastSignInTime = user.metadata?.lastSignInTimestamp ?: 0L
        val currentTime = System.currentTimeMillis()

        // Tính ra số phút chênh lệch
        val timeDiffMinutes = (currentTime - lastSignInTime) / (1000 * 60)

        // Nếu đã trôi qua quá 4 phút (Lấy mốc an toàn dưới 5 phút của Firebase)
        if (timeDiffMinutes >= 4) {
            // Chặn đứng ngay lập tức, văng lỗi ra màn hình và kết thúc hàm
            // Firestore lúc này được bảo vệ an toàn 100%
            onError("Vì lý do bảo mật, vui lòng Đăng xuất rồi Đăng nhập lại để thực hiện!")
            return
        }

        // Nếu thời gian hợp lệ (< 4 phút), tiến hành dọn rác như bình thường
        isLoading = true
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // 1. Quét và xóa tất cả Bộ từ và Từ vựng của user này
                val setsSnapshot = db.collection("vocabulary_sets").whereEqualTo("userId", uid).get().await()
                for (setDoc in setsSnapshot.documents) {
                    val wordsSnapshot = db.collection("vocabularies").whereEqualTo("setId", setDoc.id).get().await()
                    for (wordDoc in wordsSnapshot.documents) {
                        db.collection("vocabularies").document(wordDoc.id).delete().await()
                    }
                    db.collection("vocabulary_sets").document(setDoc.id).delete().await()
                }

                // 2. Xóa Profile của user
                db.collection("users").document(uid).delete().await()

                // 3. Tiêu hủy tài khoản Auth
                user.delete().await()

                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                isLoading = false
                onError(translateAuthError(e.message ?: "Lỗi không xác định"))
            }
        }
    }
}
