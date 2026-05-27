package com.example.minlish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minlish.ui.screens.learning.FlashcardScreen
import com.example.minlish.ui.screens.learning.SrsReviewScreen
import com.example.minlish.ui.screens.vocabulary.WordSetListScreen
import com.example.minlish.ui.theme.MinLishTheme
import com.example.minlish.viewmodel.LearningViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinLishTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val learningViewModel: LearningViewModel = viewModel()

    NavHost(navController = navController, startDestination = "word_set_list") {
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
                        popUpTo("word_set_list")
                    }
                }
            }
        }
        composable("session_summary") {
            SrsReviewScreen(
                viewModel = learningViewModel,
                onNavigateHome = { navController.navigate("word_set_list") {
                    popUpTo("word_set_list") { inclusive = true }
                }},
                onLearnAnother = { navController.navigate("word_set_list") }
            )
        }
    }
}
