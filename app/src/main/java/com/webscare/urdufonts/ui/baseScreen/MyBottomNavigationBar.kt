package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.navigation.Screen

@Composable
fun MyBottomNavigationBar(navController: NavController) {

    val items = listOf(
        NavBarItem(icon = R.drawable.ic_home,       label = "Home"),
        NavBarItem(icon = R.drawable.ic_styles,     label = "Styles"),
        NavBarItem(icon = R.drawable.ic_categories, label = "Categories"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = when (currentRoute) {
        Screen.Home.route        -> 0
        Screen.styles.route      -> 1
        Screen.categories.route  -> 2
        else                     -> 0
    }

    CurvedNavBar(
        items         = items,
        selectedIndex = selectedIndex,
        onItemSelected = { index ->
            val route = when (index) {
                0 -> Screen.Home.route
                1 -> Screen.styles.route
                2 -> Screen.categories.route
                else -> Screen.Home.route
            }
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState    = true
            }
        }
    )
}