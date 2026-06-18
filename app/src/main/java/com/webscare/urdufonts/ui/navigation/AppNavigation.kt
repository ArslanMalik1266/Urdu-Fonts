package com.webscare.urdufonts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.webscare.urdufonts.ui.baseScreen.BaseScreen
import com.webscare.urdufonts.ui.baseScreen.MyBottomNavigationBar
import com.webscare.urdufonts.ui.category.CategoriesScreen
import com.webscare.urdufonts.ui.home.HomeScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel
import com.webscare.urdufonts.ui.style.StylesScreen


private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.styles.route,
    Screen.categories.route,
)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.styles.route, Screen.categories.route)

    BaseScreen(
        bottomBar = {
            if (showBottomBar) {
                MyBottomNavigationBar(navController = navController)
            }
        },
        content = {
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
                composable(Screen.styles.route) {
                    StylesScreen()
                }
                composable(Screen.categories.route) {
                    CategoriesScreen()
                }
            }
        }

    )
}