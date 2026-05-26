package com.example.minlish.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minlish.ui.screens.vocabulary.AddVocabularyScreen
import com.example.minlish.ui.screens.vocabulary.CreateSetScreen
import com.example.minlish.ui.screens.vocabulary.VocabularyListScreen
import com.example.minlish.ui.screens.vocabulary.VocabularySetScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.VocabularySet.route
    ) {

        composable(
            route = Routes.VocabularySet.route
        ) {

            VocabularySetScreen(navController)
        }

        composable(
            route = Routes.CreateSet.route
        ) {

            CreateSetScreen(navController)
        }

        composable(
            route = Routes.VocabularyList.route
        ) { backStackEntry ->

            val setId =
                backStackEntry.arguments
                    ?.getString("setId")
                    ?: ""

            VocabularyListScreen(
                navController = navController,
                setId = setId
            )
        }

        composable(
            route = Routes.AddVocabulary.route
        ) { backStackEntry ->

            val setId =
                backStackEntry.arguments
                    ?.getString("setId")
                    ?: ""

            AddVocabularyScreen(
                navController = navController,
                setId = setId
            )
        }
    }
}
