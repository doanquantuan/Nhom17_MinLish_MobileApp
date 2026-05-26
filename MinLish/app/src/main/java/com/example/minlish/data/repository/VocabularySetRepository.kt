package com.example.minlish.data.repository

import com.example.minlish.data.model.VocabularySet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VocabularySetRepository (private val db: FirebaseFirestore) {

    suspend fun createSet(set: VocabularySet) {

        val doc = db.collection("vocabulary_sets").document()

        val newSet = set.copy(id = doc.id)

        doc.set(newSet).await()
    }

    suspend fun getAllSets(): List<VocabularySet> {

        return db.collection("vocabulary_sets")
            .get()
            .await()
            .toObjects(VocabularySet::class.java)
    }
}