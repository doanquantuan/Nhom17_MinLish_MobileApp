package com.example.minlish.data.repository

import com.example.minlish.data.model.Vocabulary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VocabularyRepository (private val db: FirebaseFirestore) {

    suspend fun addWord(vocabulary: Vocabulary) {
        val doc = db.collection("vocabularies").document()

        val newWord = vocabulary.copy(id = doc.id)

        doc.set(newWord).await()
    }

    suspend fun getWordsBySet(setId: String): List<Vocabulary> {

        return db.collection("vocabularies")
            .whereEqualTo("setId", setId)
            .get()
            .await()
            .toObjects(Vocabulary::class.java)
    }
}