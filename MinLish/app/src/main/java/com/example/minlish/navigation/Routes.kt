package com.example.minlish.navigation

sealed class Routes(val route: String) {

    object VocabularySet :
        Routes("vocabulary_set")

    object CreateSet :
        Routes("create_set/{setId}") {
        fun passSetId(setId: String = "new"): String {
            return "create_set/$setId"
        }
    }

    object VocabularyList :
        Routes("vocabulary_list/{setId}") {
        fun passSetId(setId: String): String {
            return "vocabulary_list/$setId"
        }
    }

    object AddVocabulary :
        Routes("add_vocabulary/{setId}?vocabularyId={vocabularyId}") {
        fun passArgs(setId: String, vocabularyId: String? = null): String {
            return if (vocabularyId != null) {
                "add_vocabulary/$setId?vocabularyId=$vocabularyId"
            } else {
                "add_vocabulary/$setId"
            }
        }
    }

    object VocabularyDetail :
        Routes("vocabulary_detail/{vocabularyId}") {
        fun passId(vocabularyId: String): String {
            return "vocabulary_detail/$vocabularyId"
        }
    }
}
