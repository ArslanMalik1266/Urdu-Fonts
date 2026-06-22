package com.webscare.urdufonts.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onCloseDrawer = { scope.launch { drawerState.close() } },
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
                            onFontClick = { fontId ->
                                navController.navigate(Screen.fontDetail.createRoute(fontId))
                            }
                        )
                    }
                    composable(Screen.styles.route) {
                        StylesScreen(
                            onCartClick = { },
                            onStyleClick = { styleItem ->
                                navController.navigate(
                                    Screen.fontListScreen.createRoute(
                                        filterType = "style",
                                        filterValue = styleItem.slug,
                                        title = "${styleItem.title} Fonts"
                                    )
                                )
                            }
                        )
                    }
                    composable(Screen.categories.route) {
                        CategoriesScreen(
                            onCartClick = { },
                            onCategoryClick = { categoryItem ->
                                navController.navigate(
                                    Screen.fontListScreen.createRoute(
                                        filterType = "category",
                                        filterValue = categoryItem.slug,
                                        title = "${categoryItem.title} Fonts"
                                    )
                                )
                            }
                        )
                    }
                    composable(
                        route = Screen.fontDetail.route,
                        arguments = listOf(
                            navArgument("fontId") {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        FontDetailScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = Screen.fontListScreen.route,
                        arguments = listOf(
                            navArgument("filterType") { type = NavType.StringType },
                            navArgument("filterValue") { type = NavType.StringType },
                            navArgument("title") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: "Urdu Fonts"
                        FontListScreen(
                            title = title,
                            onBackClick = { navController.popBackStack() },
                            onFontClick = { fontId ->
                                navController.navigate(Screen.fontDetail.createRoute(fontId))
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