package com.urdufonts.app.ui.baseScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.urdufonts.app.R
import com.urdufonts.app.ui.navigation.Screen

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val items = remember {
        listOf(
            NavBarItem(screen = Screen.Home, icon = R.drawable.ic_home, label = "Home"),
            NavBarItem(screen = Screen.styles, icon = R.drawable.ic_styles, label = "Styles"),
            NavBarItem(screen = Screen.categories, icon = R.drawable.ic_categories, label = "Categories"),
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var activeIndex by remember { mutableStateOf(0) }
    LaunchedEffect(currentRoute) {
        val index = items.indexOfFirst { it.screen.route == currentRoute }
        if (index != -1) {
            activeIndex = index // Only update if it's one of the main tabs, retaining the index on sub-screens!
        }
    }

    SimpleBottomNavigationBar(
        modifier = modifier,
        items = items,
        selectedIndex = activeIndex,
        onItemSelected = { index ->
            val selectedItem = items.getOrNull(index)
            if (selectedItem != null) {
                navController.navigate(selectedItem.screen.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )
}
