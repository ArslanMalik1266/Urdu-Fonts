package com.webscare.urdufonts.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.RegisterUserUseCase
import com.webscare.urdufonts.domain.models.RegisterParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val registerUserUseCase: RegisterUserUseCase // 👈 Injected Auth UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        // We will implement Login here later
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun onLogoutClick() {
        _uiState.update {
            ProfileUiState(isLoggedIn = false)
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
                errorMessage = null
            )
        }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
