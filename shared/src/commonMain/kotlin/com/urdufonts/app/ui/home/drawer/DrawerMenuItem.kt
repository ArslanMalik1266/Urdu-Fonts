package com.urdufonts.app.ui.home.drawer

import org.jetbrains.compose.resources.DrawableResource
import com.urdufonts.app.resources.*

sealed class DrawerMenuItem(
    val id: String,
    val label: String,
    val iconRes: DrawableResource
) {
    data object Profile : DrawerMenuItem(
        id = "profile",
        label = "Profile",
        iconRes = Res.drawable.ic_drawer_profile
    )

    data object Fonts : DrawerMenuItem(
        id = "fonts",
        label = "Fonts",
        iconRes = Res.drawable.ic_weights
    )


    data object PremiumFonts : DrawerMenuItem(
        id = "premium_fonts",
        label = "My Premium Fonts",
        iconRes = Res.drawable.ic_downloaded
    )

    data object Support : DrawerMenuItem(
        id = "support",
        label = "Support",
        iconRes = Res.drawable.ic_drawer_support
    )

    data object PrivacyPolicy : DrawerMenuItem(
        id = "privacy_policy",
        label = "Privacy Policy",
        iconRes = Res.drawable.ic_drawer_privacy
    )

    data object RateUs : DrawerMenuItem(
        id = "rate_us",
        label = "Rate Us",
        iconRes = Res.drawable.ic_drawer_rate
    )

    companion object {
        val all: List<DrawerMenuItem> = listOf(
            Profile, Fonts, PremiumFonts, Support, PrivacyPolicy, RateUs
        )
    }
}