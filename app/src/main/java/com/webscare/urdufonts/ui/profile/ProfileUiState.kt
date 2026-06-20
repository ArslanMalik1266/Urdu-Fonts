package com.webscare.urdufonts.ui.profile

data class ProfileUiState(
    val isLoggedIn: Boolean = true,
    val userName: String = "Arslan Malik",
    val email: String = "arslanmalik1262@gmail.com",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)