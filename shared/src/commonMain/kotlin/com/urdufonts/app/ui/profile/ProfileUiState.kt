package com.urdufonts.app.ui.profile

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
    val isAppleLoading: Boolean = false,
    /** Sign in with Apple is offered (iOS only). */
    val showAppleSignIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val navigateToHome: Boolean = false,
    /** Where the forgot-password flow is; null when it is not open. */
    val resetStep: ResetStep? = null,
    val resetEmail: String = "",
    /** Proof from the emailed reset code, sent with the new password. */
    val resetToken: String = ""
)

enum class ResetStep { EMAIL, CODE, NEW_PASSWORD }