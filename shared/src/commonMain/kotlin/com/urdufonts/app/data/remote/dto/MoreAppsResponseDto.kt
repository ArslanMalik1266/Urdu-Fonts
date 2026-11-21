package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MoreAppDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String?,
    @SerialName("playstore_link") val playstoreLink: String?
)

@Serializable
data class MoreAppCategoryDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String?,
    @SerialName("apps") val apps: List<MoreAppDto>?
)

@Serializable
data class MoreAppsResponseDto(
    @SerialName("status") val status: Boolean,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<MoreAppCategoryDto>?
)
