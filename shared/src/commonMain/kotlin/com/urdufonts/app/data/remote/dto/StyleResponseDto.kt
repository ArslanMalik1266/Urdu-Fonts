package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class StyleResponseDto(
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<StyleDto> = emptyList()
)

@Serializable
data class StyleDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String?,
    @SerialName("slug") val slug: String?,
    @SerialName("description") val description: String?,
    @SerialName("thumbnail") val thumbnail: String?,
    @SerialName("seo_title") val seoTitle: String?,
    @SerialName("seo_description") val seoDescription: String?,
    @SerialName("status") val status: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("updated_at") val updatedAt: String?
)