package com.example.minlish.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

import com.example.minlish.data.model.Notification
import com.example.minlish.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Đến giờ học rồi!"
        val message = inputData.getString("message") ?: "Hôm nay bạn có từ mới cần học đó."
        val reminderTime = inputData.getString("reminderTime")
        
        NotificationHelper.showStudyReminder(applicationContext, title, message)

        // Lưu thông báo vào Firestore để hiển thị trong App
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val repo = NotificationRepository(db)
            val notification = Notification(
                userId = userId,
                title = title,
                description = message,
                type = "study",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            runBlocking {
                repo.addNotification(notification)
            }
        }

        // Tự động lập lịch cho ngày tiếp theo nếu có reminderTime
        reminderTime?.let {
            ReminderManager.scheduleReminder(applicationContext, it)
        }
        
        return Result.success()
    }
}
