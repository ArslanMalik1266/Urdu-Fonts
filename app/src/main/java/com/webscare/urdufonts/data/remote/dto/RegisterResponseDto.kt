package com.webscare.urdufonts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserDto,
    @SerializedName("role") val role: String
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)
