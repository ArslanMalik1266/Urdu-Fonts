package com.urdufonts.app.domain.models

import org.jetbrains.compose.resources.DrawableResource
data class PremiumFeature(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: DrawableResource
)
