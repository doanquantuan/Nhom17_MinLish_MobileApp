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

    suspend fun updateWord(vocabulary: Vocabulary) {
        db.collection("vocabularies")
            .document(vocabulary.id)
            .set(vocabulary)
            .await()
    }

    suspend fun deleteWord(wordId: String) {
        db.collection("vocabularies")
            .document(wordId)
            .delete()
            .await()
    }

    suspend fun getWordById(wordId: String): Vocabulary? {
        return db.collection("vocabularies")
            .document(wordId)
            .get()
            .await()
            .toObject(Vocabulary::class.java)
    }

    suspend fun getWordCountBySet(setId: String): Int {
        return try {
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .get()
                .await()
            result.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getReviewCountBySet(setId: String): Int {
        return try {
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .get()
                .await()
                .toObjects(Vocabulary::class.java)
            result.count { it.status != "Mới" }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getNewCountBySet(setId: String): Int {
        return try {
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .whereEqualTo("status", "Mới")
                .get()
                .await()
            result.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getLearnedCountBySet(setId: String): Int {
        return try {
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .whereEqualTo("status", "Thuộc")
                .get()
                .await()
            result.size()
        } catch (e: Exception) {
            android.util.Log.e("VocabRepo", "Error getting learned count", e)
            0
        }
    }
}
