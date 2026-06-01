package com.example.minlish.data.repository

import com.example.minlish.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(private val db: FirebaseFirestore) {
    
    suspend fun getNotificationsByUserId(userId: String): List<Notification> {
        return try {
            android.util.Log.d("NotificationRepo", "Fetching notifications for user: $userId")
            val snapshot = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            android.util.Log.d("NotificationRepo", "Found ${snapshot.size()} documents")
            
            val list = snapshot.toObjects(Notification::class.java).mapIndexed { index, notification ->
                notification.copy(id = snapshot.documents[index].id)
            }
            
            // Sắp xếp thủ công bằng code thay vì dùng orderBy của Firestore để tránh lỗi thiếu Index
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Error fetching notifications", e)
            emptyList()
        }
    }

    fun getNotificationsFlow(userId: String): Flow<List<Notification>> = callbackFlow {
        val registration = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("NotificationRepo", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Notification::class.java).mapIndexed { index, notification ->
                        notification.copy(id = snapshot.documents[index].id)
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            android.util.Log.d("NotificationRepo", "Marking notification as read: $notificationId")
            db.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            android.util.Log.d("NotificationRepo", "Successfully marked as read")
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Error marking notification as read", e)
        }
    }

    suspend fun addNotification(notification: Notification) {
        try {
            db.collection("notifications").add(notification).await()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Error adding notification", e)
        }
    }

    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Error getting unread count", e)
            0
        }
    }

    fun getUnreadCountFlow(userId: String): Flow<Int> = callbackFlow {
        val registration = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("NotificationRepo", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { registration.remove() }
    }
}
