package com.example.minlish.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Đến giờ học rồi!"
        val message = inputData.getString("message") ?: "Hôm nay bạn có từ mới cần học đó."
        
        NotificationHelper.showStudyReminder(applicationContext, title, message)
        
        return Result.success()
    }
}
