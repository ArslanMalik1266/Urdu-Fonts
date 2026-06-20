package com.webscare.urdufonts.ui.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        // hook your auth use case here
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun onLogoutClick() {
        _uiState.update {
            ProfileUiState() // reset to logged-out defaults
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