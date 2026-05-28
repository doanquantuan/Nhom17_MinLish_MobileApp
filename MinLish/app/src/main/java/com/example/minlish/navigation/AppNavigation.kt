package com.example.minlish.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.minlish.ui.screens.auth.LoginScreen
import com.example.minlish.ui.screens.auth.RegisterScreen
import com.example.minlish.ui.screens.dashboard.DashboardScreen
import com.example.minlish.ui.screens.auth.OnboardingScreen
import com.example.minlish.ui.screens.learning.FlashcardScreen
import com.example.minlish.ui.screens.learning.SrsReviewScreen
import com.example.minlish.ui.screens.vocabulary.WordSetListScreen
import com.example.minlish.viewmodel.LearningViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val learningViewModel: LearningViewModel = viewModel()
    
    val startingPage = if (currentUser != null) "dashboard" else "login"

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

        composable("dashboard") {
            DashboardScreen(navController)
        }

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
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            }
        }

        composable("session_summary") {
            SrsReviewScreen(
                viewModel = learningViewModel,
                onNavigateHome = { 
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onLearnAnother = { 
                    navController.navigate("dashboard") 
                }
            )
        }
    }
}
