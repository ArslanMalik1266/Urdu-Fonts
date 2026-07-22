package com.webscare.urdufonts.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.webscare.urdufonts.data.local.UserPreferences
import com.webscare.urdufonts.ui.fontList.FontListScreen
import com.webscare.urdufonts.ui.baseScreen.BaseScreen
import com.webscare.urdufonts.ui.baseScreen.MyBottomNavigationBar
import com.webscare.urdufonts.ui.category.CategoriesScreen
import com.webscare.urdufonts.ui.category.CategoriesViewModel
import com.webscare.urdufonts.ui.detailScreen.FontDetailScreen
import com.webscare.urdufonts.ui.fontList.FontListViewModel
import com.webscare.urdufonts.ui.home.HomeScreen
import com.webscare.urdufonts.ui.home.HomeViewModel
import com.webscare.urdufonts.ui.home.drawer.AppDrawerContent
import com.webscare.urdufonts.ui.home.drawer.DrawerMenuItem
import com.webscare.urdufonts.ui.onboarding.OnboardingScreen
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel
import com.webscare.urdufonts.ui.profile.ProfileScreen
import com.webscare.urdufonts.ui.style.StylesScreen
import com.webscare.urdufonts.ui.style.StylesViewModel
import com.webscare.urdufonts.ui.util.BlurOverlay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


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
    val userPreferences: UserPreferences = koinInject()
    val context = LocalContext.current
    val externalNavigator = remember(context) { ExternalNavigator(context) }
    val isOnboardingCompleted by remember { userPreferences.isOnboardingCompleted }
        .collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in bottomBarRoutes
    if (isOnboardingCompleted == null) {
        // You can show a blank screen or a loading splash here
        return
    }
    val startDestination = if (isOnboardingCompleted == true) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == Screen.Home.route,
        drawerContent = {
            AppDrawerContent(
                onCloseDrawer = { scope.launch { drawerState.close() } },

                onMenuItemClick = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        is DrawerMenuItem.Profile -> navController.navigate("profile")
                        is DrawerMenuItem.Support -> externalNavigator.openEmailSupport()
                        is DrawerMenuItem.PrivacyPolicy -> externalNavigator.openWebPage("")
                        is DrawerMenuItem.RateUs -> externalNavigator.openWebPage("")
                        else -> {}
                    }
                },
                onLoginClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("profile")
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (showBottomBar) {
                BlurOverlay(modifier = Modifier.fillMaxSize())
            }
            BaseScreen(
                bottomBarVisible = showBottomBar,
                bottomBar = {
                    MyBottomNavigationBar(navController = navController)
                },
                content = {

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.Onboarding.route) {
                            val viewModel: OnboardingViewModel = koinViewModel()
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
                            val viewModel: HomeViewModel = koinViewModel()
                            HomeScreen(
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onFontClick = { fontId ->
                                    navController.navigate(Screen.fontDetail.createRoute(fontId))
                                    viewModel.clearSearch()
                                }
                            )
                        }
                        composable(Screen.styles.route) {
                            val viewModel: StylesViewModel = koinViewModel()
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
                                    viewModel.clearSearch()
                                }
                            )
                        }
                        composable(Screen.categories.route) {
                            val viewModel: CategoriesViewModel = koinViewModel()
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
                                    viewModel.clearSearch()
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
                            val viewModel: FontListViewModel = koinViewModel()
                            FontListScreen(
                                title = title,
                                onBackClick = { navController.popBackStack() },
                                onFontClick = { fontId ->
                                    navController.navigate(Screen.fontDetail.createRoute(fontId))
                                    viewModel.clearSearch()
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
}