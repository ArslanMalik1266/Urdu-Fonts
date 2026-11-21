package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUserResponseDto(
    val exists: Boolean? = null,
    val message: String? = null
)
