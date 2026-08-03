package com.urdufonts.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryResponseDto(
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<CategoryDto> = emptyList()
)

data class CategoryDto(
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