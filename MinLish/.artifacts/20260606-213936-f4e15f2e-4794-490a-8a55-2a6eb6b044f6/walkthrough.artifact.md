# Walkthrough - Fix Dashboard Statistics Discrepancy

I have fixed the issue where the "Học mới" (New Learned) count on the dashboard was incorrect after studying new words.

## Changes Made

### 1. SM-2 Algorithm Graduation Fix
- Modified [Sm2Algorithm.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/app/src/main/java/com/example/minlish/utils/Sm2Algorithm.kt) to ensure that **any** answer (AGAIN, HARD, GOOD, or EASY) results in the word's `repetitions` count becoming at least 1.
- Previously, choosing HARD (Quality 1) or AGAIN (Quality 0) for a brand-new word would sometimes keep `repetitions` at 0, preventing it from being correctly counted as "learned today" or causing it to stay in the "New" bucket indefinitely.

### 2. Dashboard Statistics De-duplication
- Updated [DashboardViewModel.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/app/src/main/java/com/example/minlish/viewmodel/DashboardViewModel.kt) to use `distinctBy { it.id }` when calculating statistics.
- This prevents inflated counts if the same word document accidentally appears multiple times in the list fetched from Firestore.

### 3. Enhanced Logging
- Added detailed logs to [DashboardViewModel.kt](file:///D:/ltdd/project/Nhom17_MinLish_MobileApp/MinLish/app/src/main/java/com/example/minlish/viewmodel/DashboardViewModel.kt) to track exactly which words are counted as "New Today" and verify the unique word count versus the raw word count.

## Verification Results

### Manual Verification Path
1.  **SM-2 Graduation**: When a new word is studied and "HARD" is selected, the log will now show `SAVING WORD: [word] | STATUS: Ôn lại | REPS: 1`. Previously, REPS would have been 0.
2.  **Dashboard Count**: Since `repetitions` is now 1 and `firstReviewedAt` is set, the word is correctly included in the `newLearnedToday` count: `COUNTED AS NEW TODAY: [word] (ID: ...) firstReviewedAt=...`.
3.  **De-duplication**: Logs now show the number of unique words vs raw words found: `REFRESHING: Found X sets and Y unique words (from Z raw)`.

## Conclusion
The dashboard statistics should now accurately reflect your learning progress, ensuring every new word you interact with is counted towards your daily goal.
