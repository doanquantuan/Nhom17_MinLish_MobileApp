package com.example.minlish.navigation

sealed class Routes(val route: String) {

    object VocabularySet :
        Routes("vocabulary_set")

    object CreateSet :
        Routes("create_set")

    object VocabularyList :
        Routes("vocabulary_list/{setId}") {

        fun passSetId(setId: String): String {
            return "vocabulary_list/$setId"
        }
    }

    object AddVocabulary :
        Routes("add_vocabulary/{setId}") {

        fun passSetId(setId: String): String {
            return "add_vocabulary/$setId"
        }
    }
}