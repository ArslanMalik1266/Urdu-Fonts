package com.urdufonts.app.domain.models

data class AuthResult(
    val message: String,
    val user: User,
    val role: String
)
