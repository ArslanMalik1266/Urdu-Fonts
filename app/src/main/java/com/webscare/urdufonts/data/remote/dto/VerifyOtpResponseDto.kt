package com.webscare.urdufonts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: VerifyOtpUserDto
)

data class VerifyOtpUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("roles") val roles: List<String>?
)
