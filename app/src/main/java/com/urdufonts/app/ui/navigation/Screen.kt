package com.urdufonts.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object styles : Screen("styles")
    object categories : Screen("categories")
    object fontDetail : Screen("fontDetail/{fontId}") {
        fun createRoute(fontId: String) = "fontDetail/$fontId"
    }
    object fontListScreen : Screen("fontListScreen/{filterType}/{filterValue}/{title}") {
        fun createRoute(filterType: String, filterValue: String, title: String) =
            "fontListScreen/$filterType/$filterValue/$title"
    }
    object Profile : Screen("profile")
    object Subscription : Screen("subscription")
    object Settings : Screen("settings")
}