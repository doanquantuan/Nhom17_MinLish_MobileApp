package com.example.minlish.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.minlish.ui.screens.vocabulary.*

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
            route = Routes.VocabularyList.route,
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val setId = backStackEntry.arguments?.getString("setId") ?: ""

            VocabularyListScreen(
                navController = navController,
                setId = setId
            )
        }

        composable(
            route = Routes.AddVocabulary.route,
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType },
                navArgument("vocabularyId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            val vocabularyId = backStackEntry.arguments?.getString("vocabularyId")

            AddVocabularyScreen(
                navController = navController,
                setId = setId,
                vocabularyId = vocabularyId
            )
        }

        composable(
            route = Routes.VocabularyDetail.route,
            arguments = listOf(
                navArgument("vocabularyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vocabularyId = backStackEntry.arguments?.getString("vocabularyId") ?: ""

            VocabularyDetailScreen(
                navController = navController,
                vocabularyId = vocabularyId
            )
        }
    }
}
