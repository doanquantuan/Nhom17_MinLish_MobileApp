package com.example.minlish.utils

import com.example.minlish.model.Quality
import com.example.minlish.model.VocabWord
import com.example.minlish.model.WordStatus
import java.util.Calendar
import java.util.Date

object Sm2Algorithm {
    fun calculateNextReview(word: VocabWord, quality: Quality): VocabWord {
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
        } else if (q == 1) {
            i = 1
        } else {
            // Quality.AGAIN: reset progress but keep in Review cycle
            n = 1
            i = 1
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
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, i)
        
        return word.copy(
            repetitions = n,
            easeFactor = ef,
            interval = i,
            nextReview = calendar.time,
            lastReviewed = Date(),
            firstReviewedAt = word.firstReviewedAt ?: if (isFirstTime) Date() else null,
            status = if (q < 2) WordStatus.REVIEW
                     else WordStatus.MASTERED
        )
    }
}
