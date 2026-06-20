package com.webscare.urdufonts.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.webscare.urdufonts.ui.fontList.FontListScreen
import com.webscare.urdufonts.ui.baseScreen.BaseScreen
import com.webscare.urdufonts.ui.baseScreen.MyBottomNavigationBar
import com.webscare.urdufonts.ui.category.CategoriesScreen
import com.webscare.urdufonts.ui.detailScreen.FontDetailScreen
import com.webscare.urdufonts.ui.home.HomeScreen
import com.webscare.urdufonts.ui.home.drawer.AppDrawerContent
import com.webscare.urdufonts.ui.home.drawer.DrawerMenuItem
import com.webscare.urdufonts.ui.onboarding.OnboardingScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel
import com.webscare.urdufonts.ui.profile.ProfileScreen
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
            AppDrawerContent(
                onCloseDrawer   = { scope.launch { drawerState.close() } },
                onMenuItemClick = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        is DrawerMenuItem.Profile -> navController.navigate("profile")
                        else -> {}
                    }
                },
                onLoginClick = { navController.navigate("profile") }
            )
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
                        StylesScreen(
                            onCartClick = { },
                            onStyleClick = {
                                navController.navigate(Screen.fontListScreen.route)
                            }
                        )
                    }
                    composable(Screen.categories.route) {
                        CategoriesScreen(
                            onCartClick = { },
                            onCategoryClick = {
                                navController.navigate(Screen.fontListScreen.route)
                            }
                        )
                    }
                    composable(Screen.fontDetail.route) {
                        FontDetailScreen()
                    }
                    composable(Screen.fontListScreen.route) {
                        FontListScreen(
                            title = "Urdu Fonts",
                            onBackClick = { navController.popBackStack() },
                            onFontClick = {
                                navController.navigate(Screen.fontDetail.route)
                            }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }

        )
    }
}