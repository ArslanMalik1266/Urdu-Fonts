package com.urdufonts.app.ui.onboarding.model

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val hasFloatingAnimation: Boolean = false,
    val dotsImageRes: Int? = null,
    val dotCount: Int = 5
)