package com.webscare.urdufonts.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.RegisterUserUseCase
import com.webscare.urdufonts.domain.usecases.GoogleSignInUseCase
import com.webscare.urdufonts.domain.usecases.LoginWithGoogleUseCase
import com.webscare.urdufonts.domain.usecases.GetUserSessionUseCase
import com.webscare.urdufonts.domain.usecases.LogoutUseCase
import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
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
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onGoogleSignInClick(context: Context) {
        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            googleSignInUseCase(context)
                .onSuccess { googleUser ->
                    loginWithGoogleUseCase(googleUser.idToken)
                        .onFailure { error ->
                            android.util.Log.e("GoogleSignIn", "ViewModel: Backend Google Login failed: ${error.message}", error)
                            onError(error.localizedMessage ?: "Backend authentication failed")
                        }
                }
                .onFailure { error ->
                    android.util.Log.e("GoogleSignIn", "ViewModel: Google Sign-in flow failure: ${error.message}", error)
                    onError(error.localizedMessage ?: "Google Sign-In failed")
                }
        }
    }

    fun onLoginClick(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and Password are required")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun toggleAuthMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, errorMessage = null) }
    }

    fun onSignUpClick(name: String, email: String, pass: String, confirmPass: String) {
        // 1. Client-side input validation
        if (name.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            onError("All fields are required")
            return
        }
        if (pass != confirmPass) {
            onError("Passwords do not match")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // 2. Launch coroutine to execute registration in UseCase
        viewModelScope.launch {
            val params = RegisterParams(name = name, email = email, pass = pass)

            registerUserUseCase(params)
                .onSuccess { authResult ->
                    // Switch UI to logged-in with username and email returned by server
                    onLoginSuccess(userName = authResult.user.name, email = authResult.user.email)
                }
                .onFailure { error ->
                    onError(error.localizedMessage ?: "Registration failed")
                }
        }
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
        _uiState.update { it.copy(isLoading = false, isGoogleLoading = false, errorMessage = message) }
    }
}
