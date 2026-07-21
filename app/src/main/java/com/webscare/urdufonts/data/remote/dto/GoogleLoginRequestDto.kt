package com.webscare.urdufonts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequestDto(
    @SerializedName("id_token") val idToken: String
)
