package com.urdufonts.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.AppColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Image(
                painter = painterResource(id = R.drawable.home_logo),
                contentDescription = "Logo",
                modifier = Modifier.wrapContentHeight()
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                TopBarButton(
                    iconRes = R.drawable.ic_settings,
                    onClick = onMenuClick,
                    contentDescription = "Settings"
                )
                TopBarButton(
                    iconRes = R.drawable.ic_diamond,
                    onClick = onSubscriptionClick,
                    contentDescription = "Subscription",
                    tint = AppColor
                )
            }
        }
    )
}