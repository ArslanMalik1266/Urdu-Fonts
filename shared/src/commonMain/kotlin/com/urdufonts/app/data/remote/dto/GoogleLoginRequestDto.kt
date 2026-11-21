package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GoogleLoginRequestDto(
    @SerialName("id_token") val idToken: String
)
