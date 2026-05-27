package com.example.minlish.utils

import com.example.minlish.model.Quality
import com.example.minlish.model.VocabWord
import com.example.minlish.model.WordStatus
import java.util.Calendar
import java.util.Date
import kotlin.math.max

object Sm2Algorithm {
    /**
     * Calculates the next review date and updated SM-2 parameters for a word.
     * Based on SM-2 algorithm: https://en.wikipedia.org/wiki/SuperMemo_2
     * 
     * @param word The current vocabulary word with its parameters.
     * @param quality The user's self-assessment quality (0-3 in this implementation).
     * @return Updated VocabWord.
     */
    fun calculateNextReview(word: VocabWord, quality: Quality): VocabWord {
        var n = word.repetitions
        var ef = word.easeFactor
        var i = word.interval
        
        val q = quality.value // 0 to 3

        if (q >= 2) { // Good or Easy (using q >= 2 as threshold for "correct" response)
            if (n == 0) {
                i = 1
            } else if (n == 1) {
                i = 6
            } else {
                i = (i * ef).toInt()
            }
            n++
        } else { // Again or Hard (reset or penalty)
            n = 0
            i = 1
        }

        // Adjust Ease Factor (modified formula to fit 0-3 scale if necessary, 
        // but typically SM-2 uses 0-5. Let's map 0-3 to something reasonable or keep it simple)
        // Original EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        // For 0-3 scale, we can map:
        // AGAIN(0) -> 1
        // HARD(1) -> 2
        // GOOD(2) -> 3
        // EASY(3) -> 5
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
            status = if (n > 5) WordStatus.MASTERED else if (n > 0) WordStatus.REVIEW else WordStatus.LEARNING
        )
    }
}
