package com.example.vetcarepro

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.vetcarepro.presentation.navigation.VetCareNavGraph
import com.example.vetcarepro.ui.theme.VetCareProTheme

@Composable
fun VetCareApp() {
    VetCareProTheme {
        VetCareNavGraph(navController = rememberNavController())
    }
}

