package com.example.minlish.data.repository

import com.example.minlish.data.model.StudySession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class StatsRepository(private val db: FirebaseFirestore) {

    suspend fun saveSession(session: StudySession) {
        val doc = db.collection("study_sessions").document()
        db.collection("study_sessions").document(doc.id)
            .set(session.copy(id = doc.id))
            .await()
    }

    suspend fun getSessionsByUserId(userId: String): List<StudySession> {
        return try {
            db.collection("study_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(StudySession::class.java)
                .sortedBy { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
