package com.urdufonts.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.ui.baseScreen.BaseScreen
import com.urdufonts.app.ui.baseScreen.MyBottomNavigationBar
import com.urdufonts.app.ui.category.CategoriesScreen
import com.urdufonts.app.ui.category.CategoriesViewModel
import com.urdufonts.app.ui.detailScreen.FontDetailScreen
import com.urdufonts.app.ui.fontList.FontListScreen
import com.urdufonts.app.ui.fontList.FontListViewModel
import com.urdufonts.app.ui.home.HomeScreen
import com.urdufonts.app.ui.home.HomeViewModel
import com.urdufonts.app.ui.onboarding.OnboardingScreen
import com.urdufonts.app.ui.onboarding.OnboardingViewModel
import com.urdufonts.app.ui.profile.ProfileScreen
import com.urdufonts.app.ui.settings.SettingsScreen
import com.urdufonts.app.ui.splash.SplashScreen
import com.urdufonts.app.ui.style.StylesScreen
import com.urdufonts.app.ui.style.StylesViewModel
import com.urdufonts.app.ui.subscription.SubscriptionBottomSheet
import com.urdufonts.app.ui.subscription.SubscriptionScreen
import com.urdufonts.app.ui.util.BlurOverlay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.styles.route,
    Screen.categories.route,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userPreferences: UserPreferences = koinInject()
    val context = LocalContext.current
    val externalNavigator = remember(context) { ExternalNavigator(context) }
    val isOnboardingCompleted by remember { userPreferences.isOnboardingCompleted }
        .collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in bottomBarRoutes
    var showSubscriptionBottomSheet by remember { mutableStateOf(false) }

    if (isOnboardingCompleted == null) {
        return
    }
    val startDestination = Screen.Splash.route

    if (showSubscriptionBottomSheet) {
        SubscriptionBottomSheet(
            onDismiss = { showSubscriptionBottomSheet = false },
            onNavigateToLogin = {
                showSubscriptionBottomSheet = false
                navController.navigate(Screen.Profile.route)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BlurOverlay(modifier = Modifier.fillMaxSize())
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
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            isOnboardingCompleted = isOnboardingCompleted == true,
                            onNavigateNext = {
                                val nextRoute = if (isOnboardingCompleted == true) Screen.Home.route else Screen.Onboarding.route
                                navController.navigate(nextRoute) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }
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
                            onMenuClick = { navController.navigate(Screen.Settings.route) },
                            onFontClick = { fontId ->
                                navController.navigate(Screen.fontDetail.createRoute(fontId))
                                viewModel.clearSearch()
                            },
                            onSubscriptionClick = { showSubscriptionBottomSheet = true }
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
                            },
                            onSubscriptionClick = { showSubscriptionBottomSheet = true }
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
                            },
                            onSubscriptionClick = { showSubscriptionBottomSheet = true }
                        )
                    }
                    composable(
                        route = Screen.fontDetail.route,
                        arguments = listOf(
                            navArgument("fontId") { type = NavType.StringType }
                        )
                    ) {
                        FontDetailScreen(
                            onBackClick = { navController.popBackStack() },
                            onNavigateToSubscription = { showSubscriptionBottomSheet = true }
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
                            },
                            onSubscriptionClick = { showSubscriptionBottomSheet = true }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable(Screen.Subscription.route) {
                        SubscriptionScreen(
                            onBackClick = { navController.popBackStack() },
                            onNavigateToLogin = { navController.navigate(Screen.Profile.route) }
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onNavigateToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToSubscription = { showSubscriptionBottomSheet = true },
                            onOpenWebPage = { url -> externalNavigator.openWebPage(url) },
                            onOpenEmailSupport = { externalNavigator.openEmailSupport() },
                            onRateUsClick = { externalNavigator.openPlayStoreForRating() }
                        )
                    }
                }
            }
        )
    }
}