package com.webscare.urdufonts.ui.home.drawer

data class DrawerUiState(
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userSubtitle: String = "Access premium Urdu fonts",
    val profileImageUrl: String? = null,
    val selectedItem: DrawerMenuItem? = null,
    val menuItems: List<DrawerMenuItem> = DrawerMenuItem.all,
    val appVersion: String = "1.0.0"
)