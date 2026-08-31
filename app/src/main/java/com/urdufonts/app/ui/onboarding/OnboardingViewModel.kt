package com.urdufonts.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.R
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.ui.onboarding.model.OnboardingPage
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    val pages = listOf(
        OnboardingPage(
            R.drawable.onboarding_one_image,
            "All Your Urdu Fonts",
            "Browse, preview, download & use your favourite Urdu fonts on your mobile",
            dotsImageRes = R.drawable.dots_image
        ),
        OnboardingPage(
            R.drawable.onboarding_two_screen,
            "Choose from Categories",
            "Explore variety of Urdu fonts and instantly preview them on your mobile.",
            dotsImageRes = R.drawable.dots_image,
            dotCount = 4
        ),
        OnboardingPage(
            R.drawable.onboarding_screen_three,
            "Browse Font Styles",
            "Find your perfect font arranged by styles like bold, round, thin, condensed etc.",
            hasFloatingAnimation = true
        )
    )

    fun onContinueClicked(currentPage: Int, onFinished: () -> Unit) {
        if (currentPage == pages.size - 1) {
            viewModelScope.launch {
                // userPreferences.saveOnboardingCompleted(true) // COMMENTED FOR TESTING: Allows onboarding to show every time
                onFinished()
            }
        }
    }

    fun onSkipClicked(onFinished: () -> Unit) {
        viewModelScope.launch {
            // userPreferences.saveOnboardingCompleted(true) // COMMENTED FOR TESTING: Allows onboarding to show every time
            onFinished()
        }
    }
}