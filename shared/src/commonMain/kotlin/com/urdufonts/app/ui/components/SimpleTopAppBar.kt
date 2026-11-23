package com.urdufonts.app.ui.components

import com.urdufonts.app.resources.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onCartClick: () -> Unit = {},
    onSubscriptionClick: (() -> Unit)? = null,
    showSubscriptionIcon: Boolean = true,
    containerColor: Color = Color.Transparent
) {
    TopAppBar(
        modifier = Modifier.padding(horizontal = 4.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor
        ),
        navigationIcon = {
            if (onBackClick != null) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    TopBarButton(
                        iconRes = Res.drawable.ic_back,
                        onClick = onBackClick,
                        contentDescription = "Back"
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor.copy(0.8f),
                fontFamily = NunitoFontFamily
            )
        },
        actions = {
            if (showSubscriptionIcon && onSubscriptionClick != null) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    TopBarButton(
                        iconRes = Res.drawable.ic_diamond,
                        onClick = onSubscriptionClick,
                        contentDescription = "Subscription",
                        tint = AppColor
                    )
                }
            }
        },
    )
}