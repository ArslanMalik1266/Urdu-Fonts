package com.urdufonts.app.domain.models

data class UserSession(
    val token: String,
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?,
    val role: String
)
