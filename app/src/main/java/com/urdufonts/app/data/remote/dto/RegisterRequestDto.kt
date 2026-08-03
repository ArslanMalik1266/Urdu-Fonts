package com.urdufonts.app.data.remote.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val pass: String
) {
    fun toFieldMap(): Map<String, String> {
        return mapOf(
            "name" to name,
            "email" to email,
            "password" to pass
        )
    }
}
