package com.example.minlish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.minlish.ui.screens.auth.LoginScreen
import com.example.minlish.ui.screens.auth.RegisterScreen
import com.example.minlish.ui.screens.dashboard.DashboardScreen
import com.example.minlish.ui.screens.auth.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
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
    }
}