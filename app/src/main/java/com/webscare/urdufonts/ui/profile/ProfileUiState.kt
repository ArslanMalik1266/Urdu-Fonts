package com.webscare.urdufonts.ui.profile

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val isSignUpMode: Boolean = false,
    val isOtpMode: Boolean = false,
    val registrationEmail: String = "",
    val userName: String = "Arslan Malik",
    val email: String = "arslanmalik1262@gmail.com",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val navigateToHome: Boolean = false
)