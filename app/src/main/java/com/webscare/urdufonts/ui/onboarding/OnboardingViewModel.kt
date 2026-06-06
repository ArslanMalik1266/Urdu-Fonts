package com.webscare.urdufonts.ui.onboarding

import androidx.lifecycle.ViewModel
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.onboarding.model.OnboardingPage

class OnboardingViewModel : ViewModel() {
    val pages = listOf(
        OnboardingPage(R.drawable.onboarding_screen_one, "All Your Urdu Fonts", "Browse, preview, download & use your favourite Urdu fonts on your mobile"),
        OnboardingPage(R.drawable.onboarding_screen_two, "Choose from Categories", "Explore variety of Urdu fonts and instantly preview them on your mobile."),
        OnboardingPage(R.drawable.onboarding_screen_three, "Browse Font Styles", "Find your perfect font arranged by styles like bold, round, thin, condensed etc.")
    )

    fun onContinueClicked(currentPage: Int, onFinished: () -> Unit) {
        if (currentPage == pages.size - 1) {
            onFinished()
        }
    }
    fun onSkipClicked(onFinished: () -> Unit) {
        onFinished()
    }
}