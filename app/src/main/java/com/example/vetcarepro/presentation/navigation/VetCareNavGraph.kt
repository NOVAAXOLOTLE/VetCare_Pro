package com.example.vetcarepro.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vetcarepro.presentation.screens.AppointmentCalendarScreen
import com.example.vetcarepro.presentation.screens.BranchMapScreen
import com.example.vetcarepro.presentation.screens.DashboardScreen
import com.example.vetcarepro.presentation.screens.LoginScreen
import com.example.vetcarepro.presentation.screens.MedicalHistoryScreen
import com.example.vetcarepro.presentation.screens.MultimediaCatalogScreen
import com.example.vetcarepro.presentation.screens.OfflineInformationScreen
import com.example.vetcarepro.presentation.screens.PetRegistrationScreen
import com.example.vetcarepro.presentation.screens.QrScannerScreen
import com.example.vetcarepro.presentation.screens.VaccinationControlScreen
import com.example.vetcarepro.presentation.viewmodels.AuthViewModel
import com.example.vetcarepro.presentation.viewmodels.VetCareViewModel

@Composable
fun VetCareNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val vetCareViewModel: VetCareViewModel = hiltViewModel()
    val session = authViewModel.session.collectAsStateWithLifecycle().value

    LaunchedEffect(session.isAuthenticated) {
        if (session.isAuthenticated && navController.currentDestination?.route != VetCareRoutes.Dashboard) {
            navController.navigate(VetCareRoutes.Dashboard) {
                popUpTo(VetCareRoutes.Login) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = VetCareRoutes.Login) {
        composable(VetCareRoutes.Login) {
            LoginScreen(authViewModel = authViewModel, onLoginSuccess = {
                navController.navigate(VetCareRoutes.Dashboard) {
                    popUpTo(VetCareRoutes.Login) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable(VetCareRoutes.Dashboard) {
            DashboardScreen(
                vetCareViewModel = vetCareViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(VetCareRoutes.Login) {
                        popUpTo(VetCareRoutes.Dashboard) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(VetCareRoutes.PetRegistration) {
            PetRegistrationScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(VetCareRoutes.Scanner) {
            QrScannerScreen(vetCareViewModel = vetCareViewModel) { petId ->
                navController.navigate(VetCareRoutes.medicalHistoryRoute(petId))
            }
        }
        composable(VetCareRoutes.Appointments) {
            AppointmentCalendarScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(
            route = VetCareRoutes.MedicalHistoryWithPet,
            arguments = listOf(navArgument(VetCareRoutes.PetIdArg) { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            MedicalHistoryScreen(
                vetCareViewModel = vetCareViewModel,
                petId = backStackEntry.arguments?.getString(VetCareRoutes.PetIdArg)
            )
        }
        composable(VetCareRoutes.BranchMap) {
            BranchMapScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(VetCareRoutes.Vaccinations) {
            VaccinationControlScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(VetCareRoutes.Multimedia) {
            MultimediaCatalogScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(VetCareRoutes.Offline) {
            OfflineInformationScreen(vetCareViewModel = vetCareViewModel)
        }
        composable(VetCareRoutes.MedicalHistory) {
            MedicalHistoryScreen(vetCareViewModel = vetCareViewModel, petId = null)
        }
    }
}


