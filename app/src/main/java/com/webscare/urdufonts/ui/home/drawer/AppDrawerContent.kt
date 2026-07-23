package com.webscare.urdufonts.ui.home.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.components.TopBarButton
import com.webscare.urdufonts.ui.home.HomeViewModel
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.addPressEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppDrawerContent(
    onCloseDrawer: () -> Unit,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.drawerUiState.collectAsStateWithLifecycle()

    AppDrawerContentInternal(
        uiState = uiState,
        onCloseDrawer = onCloseDrawer,
        onMenuItemClick = onMenuItemClick,
        onLoginClick = onLoginClick
    )
}


@Composable
internal fun AppDrawerContentInternal(
    uiState: DrawerUiState,
    onCloseDrawer: () -> Unit,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onLoginClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            .background(Color.White)
    ) {

        // ── Background image bottom 40% ───────────────────────────────────
        Image(
            painter = painterResource(R.drawable.drawar_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp, horizontal = 8.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────────
            DrawerHeader(
                isLoggedIn = uiState.isLoggedIn,
                userName = uiState.userName,
                userSubtitle = uiState.userSubtitle,
                profileImageUrl = uiState.profileImageUrl,
                onCloseDrawer = onCloseDrawer,
                onLoginClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Menu items ────────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                uiState.menuItems.forEach { item ->
                    DrawerMenuRow(
                        item = item,
                        isSelected = item.id == uiState.selectedItem?.id,
                        onClick    = { onMenuItemClick(item) }
                    )
                }
            }

            // ── Footer ────────────────────────────────────────────────────────────
            DrawerFooter(appVersion = uiState.appVersion)
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun DrawerHeader(
    isLoggedIn: Boolean,
    userName: String?,
    userSubtitle: String,
    profileImageUrl: String?,
    onCloseDrawer: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (isLoggedIn) {
                Image(
                    painter = if (profileImageUrl != null) {
                        rememberAsyncImagePainter(profileImageUrl)
                    } else {
                        painterResource(R.drawable.profile_image)
                    },
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_drawer_profile),
                    contentDescription = null,
                    tint = GreyColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name / login prompt
        Text(
            text = if (isLoggedIn && !userName.isNullOrBlank()) userName else "Join Urdu Fonts",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreyColor,
            textAlign = TextAlign.Center,
            fontFamily = NunitoFontFamily,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = userSubtitle,
            fontSize = 12.sp,
            color = GreyColor.copy(alpha = 0.6f),
            fontFamily = NunitoFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Login / Signup button (Only displayed when logged out)
        if (!isLoggedIn) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .addPressEffect { onLoginClick() }
                    .clip(RoundedCornerShape(50))
                    .background(AppColor)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Login / Signup",
                    color = Color.White,
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerMenuRow(
    item: DrawerMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AppColor.copy(0.2f) else Color.Transparent
    val iconTint = if (isSelected) AppColor else GreyColor
    val textColor = if (isSelected) HeadingBlackColor else GreyColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .addPressEffect(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(iconTint)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            fontFamily = NunitoFontFamily,
        )
    }
}

@Composable
private fun DrawerFooter(appVersion: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Urdu Fonts App",
                fontSize = 11.sp,
                color = DarkGreen,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "  |  ",
                fontSize = 11.sp,
                color = GreyColor.copy(alpha = 0.3f)
            )
            Text(
                text = "Version $appVersion",
                fontSize = 11.sp,
                color = GreyColor,
                fontFamily = NunitoFontFamily
            )
        }
    }

}