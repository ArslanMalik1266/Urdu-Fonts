package com.urdufonts.app.domain.models

data class GoogleUser(
    val idToken: String,
    val email: String,
    val name: String?,
    val profilePictureUri: String?
)
