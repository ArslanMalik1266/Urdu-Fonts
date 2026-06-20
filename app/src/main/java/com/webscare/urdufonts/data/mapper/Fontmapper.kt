package com.webscare.urdufonts.data.mapper

import com.webscare.urdufonts.data.remote.NetworkConstants
import com.webscare.urdufonts.data.remote.dto.FontDto
import com.webscare.urdufonts.data.remote.dto.FontClassifierDto
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.models.FontClassifier


private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return NetworkConstants.IMAGE_BASE_URL + this
}
fun FontClassifierDto.toDomain(): FontClassifier {
    return FontClassifier(
        id = id,
        title = title.orEmpty(),
        slug = slug.orEmpty(),
        description = description,
        thumbnailUrl = thumbnail.toFullImageUrl()
    )
}

fun FontDto.toDomain(): FontItem {
    return FontItem(
        id = id,
        name = title.orEmpty(),
        slug = slug.orEmpty(),
        language = language.orEmpty(),
        description = description.orEmpty(),
        developer = developer.orEmpty(),
        fontFamily = fontFamily.orEmpty(),
        tags = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        featureImageUrl = featureImage.toFullImageUrl(),
        cardImageUrl = cardImage.toFullImageUrl(),
        fontFileUrl = fontFile.toFullImageUrl(),
        previewFileUrl = previewFile.toFullImageUrl(),
        weightCount = fontWeight?.toIntOrNull() ?: 1,
        categories = categories?.map { it.toDomain() },   // ← safe call
        styles = styles?.map { it.toDomain() }
    )
}