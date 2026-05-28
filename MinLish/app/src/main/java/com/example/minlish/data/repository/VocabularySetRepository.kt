package com.example.minlish.data.repository

import com.example.minlish.data.model.VocabularySet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class VocabularySetRepository (private val db: FirebaseFirestore) {

    suspend fun createSet(set: VocabularySet) {
        val doc = db.collection("vocabulary_sets").document()
        val newSet = set.copy(id = doc.id)
        doc.set(newSet).await()
    }

    suspend fun updateSet(set: VocabularySet) {
        db.collection("vocabulary_sets")
            .document(set.id)
            .set(set)
            .await()
    }

    suspend fun deleteSet(setId: String) {
        db.collection("vocabulary_sets")
            .document(setId)
            .delete()
            .await()
    }

    suspend fun getAllSets(): List<VocabularySet> {
        return db.collection("vocabulary_sets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(VocabularySet::class.java)
    }

    suspend fun getSetsByUserId(userId: String): List<VocabularySet> {
        return db.collection("vocabulary_sets")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(VocabularySet::class.java)
            .sortedByDescending { it.createdAt }
    }
}