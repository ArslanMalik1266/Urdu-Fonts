package com.webscare.urdufonts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.webscare.urdufonts.ui.home.HomeScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = viewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}