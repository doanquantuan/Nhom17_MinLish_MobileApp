package com.example.minlish.ui.screens.vocabulary

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun CreateSetScreen(
    navController: NavController
) {

    Column {

        Text("Create Set Screen")

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {

            Text("Back")
        }
    }
}