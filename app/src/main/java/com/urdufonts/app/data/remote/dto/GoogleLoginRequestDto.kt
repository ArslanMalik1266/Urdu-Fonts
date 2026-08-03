package com.urdufonts.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequestDto(
    @SerializedName("id_token") val idToken: String
)
