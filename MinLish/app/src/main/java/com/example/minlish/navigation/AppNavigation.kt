package com.example.minlish.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.minlish.ui.screens.auth.LoginScreen
import com.example.minlish.ui.screens.auth.RegisterScreen
import com.example.minlish.ui.screens.dashboard.DashboardScreen
import com.example.minlish.ui.screens.auth.OnboardingScreen
import com.example.minlish.ui.screens.learning.FlashcardScreen
import com.example.minlish.ui.screens.learning.SrsReviewScreen
import com.example.minlish.ui.screens.notification.NotificationScreen
import com.example.minlish.ui.screens.vocabulary.WordSetListScreen
import com.example.minlish.ui.screens.vocabulary.*
import com.example.minlish.viewmodel.LearningViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val learningViewModel: LearningViewModel = viewModel()
    
    val startingPage = if (currentUser != null) "dashboard/0" else "login"

    NavHost(
        navController = navController,
        startDestination = startingPage
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("onboarding") {
            OnboardingScreen(navController)
        }

        composable(
            route = "dashboard/{tabIndex}",
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            DashboardScreen(
                navController = navController,
                learningViewModel = learningViewModel,
                initialTab = tabIndex
            )
        }

        // Vocabulary feature routes
        composable(route = Routes.VocabularySet.route) {
            VocabularySetScreen(navController)
        }

        composable(route = Routes.CreateSet.route) {
            CreateSetScreen(navController)
        }

        composable(
            route = Routes.VocabularyList.route,
            arguments = listOf(navArgument("setId") { type = NavType.StringType })
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            VocabularyListScreen(navController = navController, setId = setId)
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
            AddVocabularyScreen(navController = navController, setId = setId, vocabularyId = vocabularyId)
        }

        composable(
            route = Routes.VocabularyDetail.route,
            arguments = listOf(navArgument("vocabularyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vocabularyId = backStackEntry.arguments?.getString("vocabularyId") ?: ""
            VocabularyDetailScreen(navController = navController, vocabularyId = vocabularyId)
        }

        // Learning feature routes
        composable("word_set_list") {
            WordSetListScreen(
                onNavigateToFlashcard = { deckId, isReview ->
                    learningViewModel.startSession(deckId, isReview)
                    navController.navigate("flashcard")
                },
                viewModel = learningViewModel
            )
        }

        composable("flashcard") {
            val isFinished = learningViewModel.isFinished.collectAsState()
            
            FlashcardScreen(
                viewModel = learningViewModel,
                onBack = { navController.popBackStack() }
            )
            
            if (isFinished.value) {
                LaunchedEffect(Unit) {
                    navController.navigate("session_summary") {
                        popUpTo("dashboard/0") { inclusive = false }
                    }
                }
            }
        }

        composable("session_summary") {
            SrsReviewScreen(
                viewModel = learningViewModel,
                onNavigateHome = { 
                    navController.navigate("dashboard/0") {
                        popUpTo("dashboard/0") { inclusive = true }
                    }
                },
                onLearnAnother = { 
                    navController.navigate("dashboard/2") {
                        popUpTo("dashboard/0") { inclusive = false }
                    }
                }
            )
        }

        composable("notifications") {
            NotificationScreen(navController)
        }
    }
}

