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

    suspend fun getAllWordsByUserId(userId: String): List<Vocabulary> {
        // SincesetId is related to sets owned by user, we might need a better way 
        // but for now, we can fetch all vocabularies. 
        // In a real app, vocabularies should probably have a userId field.
        // Looking at current schema, it doesn't. 
        // Let's check if we can get all words from all sets of the user.
        val sets = db.collection("vocabulary_sets")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(com.example.minlish.data.model.VocabularySet::class.java)
            
        val allWords = mutableListOf<Vocabulary>()
        for (set in sets) {
            val words = getWordsBySet(set.id)
            allWords.addAll(words)
        }
        return allWords
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
            val now = System.currentTimeMillis()
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .get()
                .await()
                .toObjects(Vocabulary::class.java)
            result.count { (it.repetitions > 0) && (it.nextReview <= now) }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getNewCountBySet(setId: String): Int {
        return try {
            val result = db.collection("vocabularies")
                .whereEqualTo("setId", setId)
                .whereEqualTo("repetitions", 0)
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
