package com.urdufonts.app.ui.profile

import com.urdufonts.app.platform.PlatformContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.RegisterUserUseCase
import com.urdufonts.app.domain.usecases.GoogleSignInUseCase
import com.urdufonts.app.domain.usecases.LoginWithGoogleUseCase
import com.urdufonts.app.domain.usecases.GetUserSessionUseCase
import com.urdufonts.app.domain.usecases.LogoutUseCase
import com.urdufonts.app.domain.usecases.VerifyOtpUseCase
import com.urdufonts.app.domain.usecases.LoginUseCase
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.repo.EntitlementRepository
import com.urdufonts.app.domain.usecases.SetPasswordUseCase

class ProfileViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val loginUseCase: LoginUseCase,
    private val setPasswordUseCase: SetPasswordUseCase,
    private val entitlementRepository: EntitlementRepository,
    private val authRepository: com.urdufonts.app.domain.repo.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(showAppleSignIn = authRepository.isAppleSignInAvailable))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserSessionUseCase().collect { session ->
                _uiState.update {
                    it.copy(
                        isLoggedIn = session != null,
                        userName = session?.name ?: "",
                        email = session?.email ?: "",
                        profileImageUrl = session?.avatar,
                        isLoading = false,
                        isGoogleLoading = false,
                        isAppleLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onGoogleSignInClick(context: PlatformContext) {
        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            googleSignInUseCase(context)
                .onSuccess { googleUser ->
                    loginWithGoogleUseCase(googleUser.idToken)
                        .onSuccess { session ->
                            if (session.email.isNotBlank()) {
                                entitlementRepository.refreshEntitlements(session.email, force = true)
                            }
                            _uiState.update { it.copy(navigateToHome = true) }
                        }
                        .onFailure { error ->
                            com.urdufonts.app.platform.AppLog.e("GoogleSignIn", "ViewModel: Backend Google Login failed: ${error.message}", error)
                            onError(error.message ?: "Backend authentication failed")
                        }
                }
                .onFailure { error ->
                    com.urdufonts.app.platform.AppLog.e("GoogleSignIn", "ViewModel: Google Sign-in flow failure: ${error.message}", error)
                    onError(error.message ?: "Google Sign-In failed")
                }
        }
    }

    fun onAppleSignInClick() {
        _uiState.update { it.copy(isAppleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signInWithApple()
                .onSuccess { session ->
                    if (session.email.isNotBlank()) {
                        entitlementRepository.refreshEntitlements(session.email, force = true)
                    }
                    _uiState.update { it.copy(isAppleLoading = false, navigateToHome = true) }
                }
                .onFailure { error ->
                    com.urdufonts.app.platform.AppLog.e("AppleSignIn", "Apple sign-in failed: ${error.message}", error)
                    onError(error.message ?: "Sign in with Apple failed")
                }
        }
    }

    fun onLoginClick(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and Password are required")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            loginUseCase(email = email, password = password)
                .onSuccess { session ->
                    if (session.email.isNotBlank()) {
                        entitlementRepository.refreshEntitlements(session.email, force = true)
                    }
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userName = session.name,
                            email = session.email,
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Logged in successfully!",
                            navigateToHome = true
                        )
                    }
                }
                .onFailure { error ->
                    onError(error.message ?: "Login failed")
                }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            logoutUseCase()
            val anonymousId = com.urdufonts.app.ui.platform.anonymousDeviceId()
            entitlementRepository.refreshEntitlements(anonymousId, force = true)
        }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun onSignUpClick(name: String, email: String, pass: String, confirmPass: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            setPasswordUseCase(
                name = name,
                email = email,
                password = pass,
                confirmPassword = confirmPass
            ).onSuccess { authResult ->
                _uiState.update {
                    it.copy(
                        isOtpMode = true,
                        registrationEmail = email,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                onError(error.message ?: "Registration failed")
            }
        }
    }

    fun onVerifyOtpClick(otp: String) {
        if (otp.isBlank()) {
            onError("OTP code is required")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            verifyOtpUseCase(email = _uiState.value.registrationEmail, otp = otp)
                .onSuccess { session ->
                    if (session.email.isNotBlank()) {
                        entitlementRepository.refreshEntitlements(session.email, force = true)
                    }
                    _uiState.update {
                        it.copy(
                            isOtpMode = false,
                            isSignUpMode = false,
                            isLoggedIn = true,
                            userName = session.name,
                            email = session.email,
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Account registered and logged in successfully!",
                            navigateToHome = true
                        )
                    }
                }
                .onFailure { error ->
                    onError(error.message ?: "Verification failed")
                }
        }
    }

    // ── Forgot password: email -> emailed code -> new password ────────────

    fun onForgotPasswordClick() {
        _uiState.update { it.copy(resetStep = ResetStep.EMAIL, resetToken = "", isLoading = false, errorMessage = null) }
    }

    fun cancelReset() {
        _uiState.update { it.copy(resetStep = null, resetToken = "", isLoading = false, errorMessage = null) }
    }

    fun onSendResetCode(email: String) {
        val clean = email.trim()
        if (!EMAIL_PATTERN.matches(clean)) {
            onError("Enter a valid email address")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.requestPasswordReset(clean)
                .onSuccess {
                    // The backend never says whether the address has an account: always move on
                    _uiState.update { it.copy(resetStep = ResetStep.CODE, resetEmail = clean, isLoading = false, errorMessage = null) }
                }
                .onFailure { error -> onError(error.message ?: "Could not send the code") }
        }
    }

    fun onVerifyResetCode(code: String) {
        if (code.length < 6) {
            onError("Enter the 6-digit code from the email")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.verifyPasswordResetCode(_uiState.value.resetEmail, code)
                .onSuccess { token ->
                    _uiState.update { it.copy(resetStep = ResetStep.NEW_PASSWORD, resetToken = token, isLoading = false, errorMessage = null) }
                }
                .onFailure { error -> onError(error.message ?: "That code is not valid") }
        }
    }

    fun onSaveNewPassword(password: String, confirm: String) {
        when {
            password.length < 8 -> return onError("Password must be at least 8 characters long.")
            password != confirm -> return onError("Passwords do not match.")
        }
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.resetPassword(state.resetEmail, state.resetToken, password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            resetStep = null,
                            resetToken = "",
                            isSignUpMode = false,
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Password changed. Log in with your new password."
                        )
                    }
                }
                .onFailure { error -> onError(error.message ?: "Could not change the password") }
        }
    }

    /** Emails a new code for whichever code screen is open. */
    fun onResendCode() {
        val state = _uiState.value
        viewModelScope.launch {
            val result = if (state.resetStep == ResetStep.CODE) {
                authRepository.requestPasswordReset(state.resetEmail)
            } else {
                authRepository.resendVerificationCode(state.registrationEmail)
            }
            result
                .onSuccess { _uiState.update { it.copy(errorMessage = null, successMessage = "A new code is on its way.") } }
                .onFailure { error -> onError(error.message ?: "Could not send a new code") }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun cancelOtp() {
        _uiState.update { it.copy(isOtpMode = false, errorMessage = null) }
    }

    fun clearNavigateToHome() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun onLoginSuccess(userName: String, email: String) {
        _uiState.update {
            it.copy(
                isLoggedIn   = true,
                userName     = userName,
                email        = email,
                isLoading    = false,
                isGoogleLoading = false,
                errorMessage = null
            )
        }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(isLoading = false, isGoogleLoading = false, isAppleLoading = false, errorMessage = message) }
    }
}

/** Same rule as Android's Patterns.EMAIL_ADDRESS. */
private val EMAIL_PATTERN = Regex("[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")
