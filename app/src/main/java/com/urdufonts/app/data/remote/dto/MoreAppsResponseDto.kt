package com.urdufonts.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MoreAppDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("playstore_link") val playstoreLink: String?
)

data class MoreAppCategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String?,
    @SerializedName("apps") val apps: List<MoreAppDto>?
)

data class MoreAppsResponseDto(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<MoreAppCategoryDto>?
)
