package com.example.minlish.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.minlish.ui.screens.auth.LoginScreen
import com.example.minlish.ui.screens.auth.RegisterScreen
import com.example.minlish.ui.screens.auth.OnboardingScreen
import com.example.minlish.ui.screens.dashboard.HomeScreen
import com.example.minlish.ui.screens.dashboard.StatisticsScreen
import com.example.minlish.ui.screens.profile.ProfileScreen
import com.example.minlish.ui.screens.learning.FlashcardScreen
import com.example.minlish.ui.screens.learning.SrsReviewScreen
import com.example.minlish.ui.screens.notification.NotificationScreen
import com.example.minlish.ui.screens.vocabulary.WordSetListScreen
import com.example.minlish.ui.screens.vocabulary.*
import com.example.minlish.ui.screens.game.QuizScreen
import com.example.minlish.ui.screens.game.MatchingScreen
import com.example.minlish.ui.components.MinLishBottomNavigation
import com.example.minlish.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Shared ViewModels for state persistence and global logic
    val authViewModel: AuthViewModel = viewModel()
    val vocabViewModel: VocabularyViewModel = viewModel()
    val learningViewModel: LearningViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val gameViewModel: GameViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabs = listOf(
        Routes.Home.route,
        Routes.VocabularySet.route,
        Routes.Study.route,
        Routes.Statistics.route,
        Routes.Profile.route
    )
    val showBottomBar = currentRoute in mainTabs

    // --- Global Refresh Logic ---
    val isFinished by learningViewModel.isFinished.collectAsState()
    val quizSession by gameViewModel.quizSession.collectAsState()
    val matchingSession by gameViewModel.matchingSession.collectAsState()

    LaunchedEffect(isFinished, quizSession?.isFinished, matchingSession?.isFinished) {
        if (isFinished || quizSession?.isFinished == true || matchingSession?.isFinished == true) {
            dashboardViewModel.refreshData()
            vocabViewModel.loadVocabularySets()
            learningViewModel.loadRealDecks()
            if (isFinished) learningViewModel.resetFinishedStatus()
            if (quizSession?.isFinished == true || matchingSession?.isFinished == true) gameViewModel.resetFinishedStatus()
        }
    }

    // Sync data on resume if in a main tab
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && currentRoute in mainTabs) {
                dashboardViewModel.refreshData()
                learningViewModel.refreshDecks()
                if (currentRoute == Routes.VocabularySet.route) vocabViewModel.loadVocabularySets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MinLishBottomNavigation(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) Routes.Home.route else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("onboarding") { OnboardingScreen(navController) }

            // --- Main Tabs ---
            composable(Routes.Home.route) {
                LaunchedEffect(Unit) {
                    dashboardViewModel.refreshData()
                }
                val data by dashboardViewModel.dashboardData.collectAsState()
                val loading by dashboardViewModel.isLoading.collectAsState()
                val unread by dashboardViewModel.unreadCount.collectAsState()
                
                if (loading && data.wordSets.isEmpty()) {
                    LoadingScreen()
                } else {
                    HomeScreen(
                        data = data,
                        displayName = authViewModel.userName,
                        unreadCount = unread,
                        primaryColor = Color(0xFF534AB7),
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToDeck = { setId -> navController.navigate(Routes.VocabularyList.passSetId(setId)) }
                    )
                }
            }

            composable(Routes.VocabularySet.route) {
                LaunchedEffect(Unit) {
                    vocabViewModel.loadVocabularySets()
                }
                val sets by vocabViewModel.vocabularySets.collectAsState()
                val counts by vocabViewModel.setWordCounts.collectAsState()
                val loading by vocabViewModel.isLoading.collectAsState()
                
                VocabularySetScreenContent(
                    vocabularySets = sets,
                    setWordCounts = counts,
                    isLoading = loading,
                    onAddSetClick = { navController.navigate(Routes.CreateSet.passSetId()) },
                    onDeleteSet = { setId ->
                        vocabViewModel.deleteVocabularySet(setId)
                        dashboardViewModel.refreshData()
                    },
                    onEditSet = { set -> navController.navigate(Routes.CreateSet.passSetId(set.id)) },
                    onSetClick = { setId -> navController.navigate(Routes.VocabularyList.passSetId(setId)) },
                    bottomBar = {}
                )
            }

            composable(Routes.Study.route) {
                LaunchedEffect(Unit) {
                    learningViewModel.refreshDecks()
                }
                WordSetListScreen(
                    onNavigateToFlashcard = { deckId, isReview ->
                        learningViewModel.startSession(deckId, isReview)
                        navController.navigate("flashcard")
                    },
                    onNavigateToQuiz = { deckId, count -> navController.navigate("quiz/$deckId/$count") },
                    onNavigateToMatching = { deckId, count -> navController.navigate("matching/$deckId/$count") },
                    viewModel = learningViewModel
                )
            }

            composable(Routes.Statistics.route) {
                LaunchedEffect(Unit) {
                    dashboardViewModel.refreshData()
                }
                val stats by dashboardViewModel.statsData.collectAsState()
                StatisticsScreen(data = stats, primaryColor = Color(0xFF534AB7))
            }

            composable(Routes.Profile.route) {
                ProfileScreen(navController, authViewModel)
            }

            // --- Secondary Routes ---
            composable(
                route = Routes.CreateSet.route,
                arguments = listOf(navArgument("setId") { defaultValue = "new" })
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getString("setId")
                CreateSetScreen(navController, vocabViewModel, if (setId == "new") null else setId)
            }

            composable(
                route = Routes.VocabularyList.route,
                arguments = listOf(navArgument("setId") { type = NavType.StringType })
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getString("setId") ?: ""
                VocabularyListScreen(navController, setId, vocabViewModel)
            }

            composable(
                route = Routes.AddVocabulary.route,
                arguments = listOf(
                    navArgument("setId") { type = NavType.StringType },
                    navArgument("vocabularyId") { nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getString("setId") ?: ""
                val vocabId = backStackEntry.arguments?.getString("vocabularyId")
                AddVocabularyScreen(navController, setId, vocabId, vocabViewModel)
            }

            composable(
                route = Routes.VocabularyDetail.route,
                arguments = listOf(navArgument("vocabularyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vocabId = backStackEntry.arguments?.getString("vocabularyId") ?: ""
                VocabularyDetailScreen(navController, vocabId, vocabViewModel)
            }

            composable("flashcard") {
                FlashcardScreen(learningViewModel, onBack = { navController.popBackStack() })
                if (learningViewModel.isFinished.collectAsState().value) {
                    LaunchedEffect(Unit) {
                        navController.navigate("session_summary") { popUpTo(Routes.Home.route) { inclusive = false } }
                    }
                }
            }

            composable("session_summary") {
                SrsReviewScreen(
                    viewModel = learningViewModel,
                    onNavigateHome = { navController.navigate(Routes.Home.route) { popUpTo(Routes.Home.route) { inclusive = true } } },
                    onLearnAnother = { navController.navigate(Routes.Study.route) { popUpTo(Routes.Home.route) { inclusive = false } } }
                )
            }

            composable("notifications") { NotificationScreen(navController) }

            composable("quiz/{setId}/{questionCount}") { backStackEntry ->
                val setId = backStackEntry.arguments?.getString("setId") ?: ""
                val count = backStackEntry.arguments?.getString("questionCount")?.toIntOrNull() ?: 10
                QuizScreen(navController, setId, count)
            }

            composable("matching/{setId}/{questionCount}") { backStackEntry ->
                val setId = backStackEntry.arguments?.getString("setId") ?: ""
                val count = backStackEntry.arguments?.getString("questionCount")?.toIntOrNull() ?: 10
                MatchingScreen(navController, setId, count)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF534AB7))
    }
}
