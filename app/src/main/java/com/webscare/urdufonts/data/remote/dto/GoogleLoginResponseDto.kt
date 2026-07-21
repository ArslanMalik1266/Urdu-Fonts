package com.webscare.urdufonts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: GoogleUserDto
)

data class GoogleUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("roles") val roles: List<String>?
)
