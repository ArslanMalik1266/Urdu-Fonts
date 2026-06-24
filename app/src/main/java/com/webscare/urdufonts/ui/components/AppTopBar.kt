package com.webscare.urdufonts.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.webscare.urdufonts.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Image(
                painter = painterResource(id = R.drawable.home_logo),
                contentDescription = "Logo",
                modifier = Modifier.wrapContentHeight()
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                TopBarButton(
                    iconRes = R.drawable.ic_drawer,
                    onClick = onMenuClick,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {

        }
    )
}