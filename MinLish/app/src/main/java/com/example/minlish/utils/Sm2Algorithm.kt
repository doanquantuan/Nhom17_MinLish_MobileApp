package com.example.minlish.utils

import com.example.minlish.data.model.Quality
import com.example.minlish.data.model.Vocabulary
import java.util.Calendar

object Sm2Algorithm {
    fun calculateNextReview(word: Vocabulary, quality: Quality): Vocabulary {
        var n = word.repetitions
        var ef = word.easeFactor
        var i = word.interval
        
        val q = quality.value
        val isFirstTime = word.repetitions == 0

        if (q >= 2) {
            if (n == 0) {
                i = 1
            } else if (n == 1) {
                i = 6
            } else {
                i = (i * ef).toInt()
            }
            n++
        } else {
            // Quality.AGAIN or Quality.HARD:
            // Graduation: any review should mark it as no longer "Brand New" (n=0)
            n = if (n == 0) 1 else n // Always at least 1 if it was 0
            i = 1 // Review tomorrow
        }

        val mappedQ = when(quality) {
            Quality.AGAIN -> 1
            Quality.HARD -> 2
            Quality.GOOD -> 4
            Quality.EASY -> 5
        }
        
        ef = ef + (0.1 - (5 - mappedQ) * (0.08 + (5 - mappedQ) * 0.02))
        if (ef < 1.3) ef = 1.3

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, i)
        
        val newStatus = if (q < 2) "Ôn lại" else "Thuộc"
        
        return word.copy(
            repetitions = n,
            easeFactor = ef,
            interval = i,
            nextReview = calendar.timeInMillis,
            lastReviewed = System.currentTimeMillis(),
            firstReviewedAt = word.firstReviewedAt ?: if (isFirstTime) System.currentTimeMillis() else null,
            status = newStatus
        )
    }
}
