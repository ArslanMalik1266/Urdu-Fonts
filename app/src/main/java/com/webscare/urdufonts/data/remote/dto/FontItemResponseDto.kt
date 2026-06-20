package com.webscare.urdufonts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FontItemResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("data") val fonts: List<FontDto>
)

data class FontDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("visibility") val visibility: String?,
    @SerializedName("price") val price: String?,
    @SerializedName("sale_price") val salePrice: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("developer") val developer: String?,
    @SerializedName("format") val format: String?,
    @SerializedName("font_family") val fontFamily: String?,
    @SerializedName("tags") val tags: String?,
    @SerializedName("feature_image") val featureImage: String?,
    @SerializedName("card_image") val cardImage: String?,
    @SerializedName("font_file") val fontFile: String?,
    @SerializedName("font_weight") val fontWeight: String?,
    @SerializedName("preview_file") val previewFile: String?,
    @SerializedName("chart_image") val chartImage: String?,
    @SerializedName("seo_title") val seoTitle: String?,
    @SerializedName("seo_description") val seoDescription: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("categories") val categories: List<FontClassifierDto>? = null,
    @SerializedName("styles") val styles: List<FontClassifierDto>? = null
)

// Shared shape for both "categories" and "styles" — identical fields in the payload.
data class FontClassifierDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("seo_title") val seoTitle: String?,
    @SerializedName("seo_description") val seoDescription: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)