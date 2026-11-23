package com.urdufonts.app.ui.onboarding.model

import org.jetbrains.compose.resources.DrawableResource
data class OnboardingPage(
    val imageRes: DrawableResource,
    val title: String,
    val subtitle: String,
    val hasFloatingAnimation: Boolean = false,
    val dotsImageRes: DrawableResource? = null,
    val dotCount: Int = 5
)