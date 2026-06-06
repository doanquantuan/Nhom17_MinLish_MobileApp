# Fix Dashboard Statistics Discrepancy

The user reported that after learning 4 words, the "Học mới" (New Learned) count on the dashboard showed 39/30 instead of the expected 40/30. Research suggests this is likely due to an inconsistency in the SM-2 algorithm implementation and potential word duplication in the statistics calculation.

## User Review Required

- **SM-2 Logic Change**: I am changing the `HARD` quality handling in `Sm2Algorithm` to be consistent with `AGAIN`. Both will now set `repetitions = 1` if it was 0, ensuring the word graduates from the "New" status and is correctly counted in daily stats.
- **De-duplication**: I will de-duplicate the word list by document ID before calculating dashboard stats to prevent inflated counts if the same word document accidentally appears multiple times.

## Proposed Changes

### Logic & Utils

#### [Sm2Algorithm.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/app/src/main/java/com/example/minlish/utils/Sm2Algorithm.kt)

- Combine `HARD` and `AGAIN` logic to ensure consistency.
- **Ensure `repetitions` is always at least 1 after any review**, so the word graduates from the "New" bucket (n=0) immediately, even if the user answers incorrectly (AGAIN) or finds it HARD.
- This ensures the word is counted as "Học mới" on the dashboard for the day it was first encountered.

```kotlin
        if (q >= 2) {
            // Success (Good/Easy)
            if (n == 0) i = 1
            else if (n == 1) i = 6
            else i = (i * ef).toInt()
            n++
        } else {
            // Failure or Hard (Again/Hard)
            // Graduation: any review should mark it as no longer "Brand New" (n=0)
            n = if (n == 0) 1 else n // Always at least 1 if it was 0
            i = 1 // Review tomorrow
        }
```

---

### ViewModel

#### [DashboardViewModel.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/app/src/main/java/com/example/minlish/viewmodel/DashboardViewModel.kt)

- De-duplicate `allWords` by ID before performing counts.
- Add more logging to track the total number of words and how many were counted in each bucket.
- Refactor `newLearnedToday` calculation to be more explicit.

```kotlin
                val allWordsRaw = allWordsDeferred.await()
                val allWords = allWordsRaw.distinctBy { it.id }

                android.util.Log.d("DashboardVM", "REFRESHING: Found ${sets.size} sets and ${allWords.size} unique words (from ${allWordsRaw.size} raw)")
```

---

### Repository

#### [VocabularyRepository.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/data/repository/VocabularyRepository.kt)

- (Optional) Optimize `getAllWordsByUserId` to use a single query if possible, but the de-duplication in ViewModel is a safer immediate fix.

## Verification Plan

### Automated Tests
- No existing unit tests for SM-2 found. I will create a small test or use logging to verify the algorithm changes.
- Command: `./gradlew test` (to ensure no regressions if I add a test).

### Manual Verification
1.  **Check SM-2 Graduation**:
    - Add a new word.
    - Study it and choose "HARD".
    - Verify `repetitions` becomes 1 in Firestore (or via logs).
    - Verify the dashboard count "Học mới" increases by 1.
2.  **Verify De-duplication**:
    - Manually trigger a dashboard refresh and check logs for "unique words" vs "raw words".
3.  **End-to-end Session**:
    - Note current "Học mới" count (e.g., 39/30).
    - Learn 1 new word.
    - Verify it becomes 40/30.
