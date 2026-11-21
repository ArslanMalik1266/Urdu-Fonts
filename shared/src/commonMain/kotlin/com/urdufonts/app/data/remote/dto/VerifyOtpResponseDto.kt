package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class VerifyOtpResponseDto(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("token") val token: String,
    @SerialName("user") val user: VerifyOtpUserDto
)

@Serializable
data class VerifyOtpUserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("roles") val roles: List<String>?
)
