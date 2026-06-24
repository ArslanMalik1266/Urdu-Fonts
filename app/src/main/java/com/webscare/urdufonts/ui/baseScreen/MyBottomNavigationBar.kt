package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.navigation.Screen

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        NavBarItem(icon = R.drawable.ic_home, label = "Home"),
        NavBarItem(icon = R.drawable.ic_styles, label = "Styles"),
        NavBarItem(icon = R.drawable.ic_categories, label = "Categories"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only updated when landing on an actual tab screen.
    // Non-tab screens (detail, profile, etc.) never touch this,
    // so the indicator stays frozen on the last tab — no reanimation on back.
    var lockedTabIndex by rememberSaveable { mutableStateOf(0) }

    when (currentRoute) {
        Screen.Home.route -> lockedTabIndex = 0
        Screen.styles.route -> lockedTabIndex = 1
        Screen.categories.route -> lockedTabIndex = 2
        // no else — non-tab routes leave lockedTabIndex untouched
    }

    CurvedNavBar(
        modifier = modifier,
        items = items,
        selectedIndex = lockedTabIndex,
        onItemSelected = { index ->
            // index is always 0, 1, or 2 — driven by the 3 items above.
            // We use lockedTabIndex as fallback so we never navigate to a wrong route.
            val route = when (index) {
                0 -> Screen.Home.route
                1 -> Screen.styles.route
                2 -> Screen.categories.route
                else -> when (lockedTabIndex) {
                    0 -> Screen.Home.route
                    1 -> Screen.styles.route
                    else -> Screen.categories.route
                }
            }
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}