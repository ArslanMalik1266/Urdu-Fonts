package com.webscare.urdufonts.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object styles : Screen("styles")
    object categories : Screen("categories")
    object fontDetail : Screen("fontDetail")
    object fontListScreen : Screen("fontListScreen")
    object Profile : Screen("profile")
}
