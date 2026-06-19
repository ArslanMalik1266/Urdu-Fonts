package com.webscare.urdufonts.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.webscare.urdufonts.ui.baseScreen.BaseScreen
import com.webscare.urdufonts.ui.baseScreen.MyBottomNavigationBar
import com.webscare.urdufonts.ui.category.CategoriesScreen
import com.webscare.urdufonts.ui.detailScreen.FontDetailScreen
import com.webscare.urdufonts.ui.home.HomeScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel
import com.webscare.urdufonts.ui.style.StylesScreen
import kotlinx.coroutines.launch


private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.styles.route,
    Screen.categories.route,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in setOf(
        Screen.Home.route,
        Screen.styles.route,
        Screen.categories.route
    )
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer Item 1", modifier = Modifier.padding(16.dp))
                Text("Drawer Item 2", modifier = Modifier.padding(16.dp))
            }
        }
    ) {
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
                        HomeScreen(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onFontClick = {
                                navController.navigate(Screen.fontDetail.route)
                            })
                    }
                    composable(Screen.styles.route) {
                        StylesScreen()
                    }
                    composable(Screen.categories.route) {
                        CategoriesScreen()
                    }
                    composable(Screen.fontDetail.route) {
                        FontDetailScreen()
                    }
                }
            }

        )
    }
}