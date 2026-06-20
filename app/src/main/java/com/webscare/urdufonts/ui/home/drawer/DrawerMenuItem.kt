package com.webscare.urdufonts.ui.home.drawer

import androidx.annotation.DrawableRes
import com.webscare.urdufonts.R

sealed class DrawerMenuItem(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    data object Profile : DrawerMenuItem(
        id = "profile",
        label = "Profile",
        iconRes = R.drawable.ic_drawer_profile
    )

    data object Fonts : DrawerMenuItem(
        id = "fonts",
        label = "Fonts",
        iconRes = R.drawable.ic_weights
    )

    data object Premium : DrawerMenuItem(
        id = "premium",
        label = "Premium",
        iconRes = R.drawable.ic_premium
    )

    data object Downloaded : DrawerMenuItem(
        id = "downloaded",
        label = "Downloaded",
        iconRes = R.drawable.ic_downloaded
    )

    data object Support : DrawerMenuItem(
        id = "support",
        label = "Support",
        iconRes = R.drawable.ic_drawer_support
    )

    data object PrivacyPolicy : DrawerMenuItem(
        id = "privacy_policy",
        label = "Privacy Policy",
        iconRes = R.drawable.ic_drawer_privacy
    )

    data object RateUs : DrawerMenuItem(
        id = "rate_us",
        label = "Rate Us",
        iconRes = R.drawable.ic_drawer_rate
    )

    companion object {
        val all: List<DrawerMenuItem> = listOf(
            Profile, Fonts, Premium, Downloaded, Support, PrivacyPolicy, RateUs
        )
    }
}