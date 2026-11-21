package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FontItemResponseDto(
    @SerialName("message") val message: String,
    @SerialName("data") val fonts: List<FontDto>
)

@Serializable
data class FontDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String?,
    @SerialName("slug") val slug: String?,
    @SerialName("language") val language: String?,
    @SerialName("status") val status: String?,
    @SerialName("visibility") val visibility: String?,
    @SerialName("price") val price: String?,
    @SerialName("sale_price") val salePrice: String?,
    @SerialName("description") val description: String?,
    @SerialName("developer") val developer: String?,
    @SerialName("format") val format: String?,
    @SerialName("font_family") val fontFamily: String?,
    @SerialName("tags") val tags: String?,
    @SerialName("feature_image") val featureImage: String?,
    @SerialName("card_image") val cardImage: String?,
    @SerialName("font_file") val fontFile: String?,
    @SerialName("font_weight") val fontWeight: String?,
    @SerialName("preview_file") val previewFile: String?,
    @SerialName("chart_image") val chartImage: String?,
    @SerialName("seo_title") val seoTitle: String?,
    @SerialName("seo_description") val seoDescription: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("updated_at") val updatedAt: String?,
    @SerialName("categories") val categories: List<FontClassifierDto>? = null,
    @SerialName("styles") val styles: List<FontClassifierDto>? = null
)

// Shared shape for both "categories" and "styles" — identical fields in the payload.
@Serializable
data class FontClassifierDto(
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