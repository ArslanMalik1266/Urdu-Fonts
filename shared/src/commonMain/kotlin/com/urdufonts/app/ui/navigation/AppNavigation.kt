package com.urdufonts.app.ui.navigation

import androidx.savedstate.read
import com.urdufonts.app.ui.platform.ExternalNavigator
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.urdufonts.app.ui.search.SearchScreen
import com.urdufonts.app.ui.settings.SettingsScreen
import com.urdufonts.app.ui.splash.SplashScreen
import com.urdufonts.app.ui.style.StylesScreen
import com.urdufonts.app.ui.style.StylesViewModel
import com.urdufonts.app.ui.subscription.SubscriptionBottomSheet
import com.urdufonts.app.ui.subscription.SubscriptionScreen
import com.urdufonts.app.ui.util.BlurOverlay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.runtime.LaunchedEffect
import com.urdufonts.app.domain.models.NotificationTapInbox
import com.urdufonts.app.domain.usecases.CompleteNotificationTapUseCase
import com.urdufonts.app.ui.support.NewTicketScreen
import com.urdufonts.app.ui.support.SupportTheme
import com.urdufonts.app.ui.support.TicketChatScreen
import com.urdufonts.app.ui.support.TicketListScreen

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
    val externalNavigator = org.koin.compose.koinInject<ExternalNavigator>()
    val isOnboardingCompleted by remember { userPreferences.isOnboardingCompleted }
        .collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in bottomBarRoutes
    var showSubscriptionBottomSheet by remember { mutableStateOf(false) }

    // A tapped notification waits here until the app is past the splash, then opens where it leads
    val tapInbox: NotificationTapInbox = koinInject()
    val completeTap: CompleteNotificationTapUseCase = koinInject()
    val pendingTap by tapInbox.pending.collectAsState()
    val pastSplash = currentRoute != null && currentRoute != Screen.Splash.route && currentRoute != Screen.Onboarding.route
    LaunchedEffect(pendingTap, pastSplash) {
        val tap = pendingTap ?: return@LaunchedEffect
        if (!pastSplash) return@LaunchedEffect
        tap.ticketId?.let { id -> navController.navigate(Screen.TicketChat.createRoute(id)) }
        completeTap(tap)
    }

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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        BlurOverlay(modifier = Modifier.fillMaxSize())
        BaseScreen(
            bottomBarVisible = showBottomBar,
            bottomBar = {
                MyBottomNavigationBar(navController = navController)
            },
            content = {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    // Explicit so iOS matches Android. CMP on iOS otherwise uses an iOS-style slide + dim, and since
                    // screens are transparent (glow shows through), both screens overlap visibly during it.
                    enterTransition = { fadeIn(tween(250)) },
                    exitTransition = { fadeOut(tween(250)) },
                    popEnterTransition = { fadeIn(tween(250)) },
                    popExitTransition = { fadeOut(tween(250)) }
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
                            onSearchClick = { navController.navigate(Screen.Search.createRoute("fonts")) },
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
                            onSearchClick = { navController.navigate(Screen.Search.createRoute("styles")) },
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
                            onSearchClick = { navController.navigate(Screen.Search.createRoute("categories")) },
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
                        route = Screen.Search.route,
                        arguments = listOf(
                            navArgument("initialTab") {
                                type = NavType.StringType
                                defaultValue = "fonts"
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val initialTab = backStackEntry.arguments?.read { getStringOrNull("initialTab") } ?: "fonts"
                        SearchScreen(
                            onBackClick = { navController.popBackStack() },
                            initialTab = initialTab,
                            onSubscriptionClick = { showSubscriptionBottomSheet = true },
                            onFontClick = { fontId ->
                                navController.navigate(Screen.fontDetail.createRoute(fontId))
                            },
                            onCategoryClick = { categoryItem ->
                                navController.navigate(
                                    Screen.fontListScreen.createRoute(
                                        filterType = "category",
                                        filterValue = categoryItem.slug,
                                        title = "${categoryItem.title} Fonts"
                                    )
                                )
                            },
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
                        val title = backStackEntry.arguments?.read { getStringOrNull("title") } ?: "Urdu Fonts"
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
                            onRateUsClick = { externalNavigator.openPlayStoreForRating() },
                            onOpenHelpSupport = { navController.navigate(Screen.Tickets.route) }
                        )
                    }

                    // ── Help & Support: list -> new conversation -> conversation ──────────
                    composable(Screen.Tickets.route) {
                        SupportTheme {
                            TicketListScreen(
                                onBack = { navController.popBackStack() },
                                onNewTicket = { navController.navigate(Screen.NewTicket.route) },
                                onOpenTicket = { id -> navController.navigate(Screen.TicketChat.createRoute(id)) },
                            )
                        }
                    }
                    composable(Screen.NewTicket.route) {
                        SupportTheme {
                            NewTicketScreen(
                                onBack = { navController.popBackStack() },
                                // A conversation just created replaces the form, so Back returns to the list
                                onCreated = { id ->
                                    navController.navigate(Screen.TicketChat.createRoute(id)) {
                                        popUpTo(Screen.Tickets.route)
                                    }
                                },
                            )
                        }
                    }
                    composable(
                        route = Screen.TicketChat.route,
                        arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val ticketId = backStackEntry.arguments?.read { getIntOrNull("ticketId") } ?: 0
                        SupportTheme {
                            TicketChatScreen(ticketId = ticketId, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        )
    }
}