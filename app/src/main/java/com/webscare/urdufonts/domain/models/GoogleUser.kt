package com.webscare.urdufonts.domain.models

data class GoogleUser(
    val idToken: String,
    val email: String,
    val name: String?,
    val profilePictureUri: String?
)
