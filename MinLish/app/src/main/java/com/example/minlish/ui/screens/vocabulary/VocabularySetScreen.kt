package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.minlish.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularySetScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MinLish - Vocabulary Sets") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.CreateSet.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Set")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to MinLish!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Start by creating your first vocabulary set.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
