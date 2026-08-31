package com.urdufonts.app.ui.baseScreen

import com.urdufonts.app.ui.navigation.Screen
data class NavBarItem(
    val screen: Screen,
    val icon: Int,
    val selectedIcon: Int = icon,
    val label: String,
)