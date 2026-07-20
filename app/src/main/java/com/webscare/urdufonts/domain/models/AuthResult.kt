package com.webscare.urdufonts.domain.models

data class AuthResult(
    val message: String,
    val user: User,
    val role: String
)
