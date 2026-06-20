package com.webscare.urdufonts.domain.models

data class FontItem(
    val id: Int,
    val name: String,
    val slug: String,
    val language: String,
    val description: String,
    val developer: String,
    val fontFamily: String = "",
    val tags: List<String>,
    val featureImageUrl: String?,
    val cardImageUrl: String?,
    val fontFileUrl: String?,
    val previewFileUrl: String?,
    val weightCount: Int,
    val categories: List<FontClassifier>?,
    val styles: List<FontClassifier>?
) {
    // Convenience for existing UI code that wants a single label (e.g. FontItemCard header)
    val primaryCategoryName: String
        get() = categories?.firstOrNull()?.title ?: ""

    val primaryStyleName: String
        get() = styles?.firstOrNull()?.title ?: ""
}

data class FontClassifier(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?
)