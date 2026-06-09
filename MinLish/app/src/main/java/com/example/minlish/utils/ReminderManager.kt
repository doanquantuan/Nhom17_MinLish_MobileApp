package com.example.minlish.utils

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderManager {
    private const val WORK_NAME = "daily_study_reminder"

    fun scheduleReminder(context: Context, reminderTime: String) {
        val timeParts = reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance()
        val now = Calendar.getInstance()
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If time is in the past, schedule for tomorrow
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - now.timeInMillis

        // Sử dụng OneTimeWorkRequest để lập lịch chính xác hơn
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "title" to "Đến giờ học rồi!",
                "message" to "Hôm nay bạn có từ mới cần học đó.",
                "reminderTime" to reminderTime // Để Worker có thể tự lập lịch tiếp cho ngày mai
            ))
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
