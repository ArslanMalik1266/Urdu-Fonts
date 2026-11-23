package com.urdufonts.app.ui.baseScreen

import org.jetbrains.compose.resources.DrawableResource
import com.urdufonts.app.ui.navigation.Screen
data class NavBarItem(
    val screen: Screen,
    val icon: DrawableResource,
    val selectedIcon: DrawableResource = icon,
    val label: String,
)