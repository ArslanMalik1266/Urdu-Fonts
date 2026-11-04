package com.urdufonts.app.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urdufonts.app.data.local.entity.FontEntity
import com.urdufonts.app.data.remote.NetworkConstants
import com.urdufonts.app.data.remote.dto.FontClassifierDto
import com.urdufonts.app.data.remote.dto.FontDto
import com.urdufonts.app.domain.models.FontClassifier
import com.urdufonts.app.domain.models.FontItem

private val gson = Gson()

private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    // The API sends "/storage/..." paths today; use absolute URLs as they are instead of double-prefixing
    if (startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)) return this
    return NetworkConstants.IMAGE_BASE_URL + (if (startsWith("/")) this else "/$this")
}

// ── Network DTO → Domain ──────────────────────────────────────────────────────

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
        weightCount = fontWeight ?: "1",
        categories = categories?.map { it.toDomain() },
        styles = styles?.map { it.toDomain() },
        isPremium = status.equals("premium", ignoreCase = true)
    )
}

// ── Domain → Room Entity ──────────────────────────────────────────────────────

fun FontItem.toEntity(): FontEntity = FontEntity(
    id             = id,
    name           = name,
    slug           = slug,
    language       = language,
    description    = description,
    developer      = developer,
    fontFamily     = fontFamily,
    tags           = tags.joinToString(","),
    featureImageUrl = featureImageUrl,
    cardImageUrl   = cardImageUrl,
    fontFileUrl    = fontFileUrl,
    previewFileUrl = previewFileUrl,
    weightCount    = weightCount,
    categoriesJson = gson.toJson(categories ?: emptyList<FontClassifier>()),
    stylesJson     = gson.toJson(styles ?: emptyList<FontClassifier>()),
    isPremium      = isPremium
)

// ── Room Entity → Domain ──────────────────────────────────────────────────────

fun FontEntity.toDomain(): FontItem {
    val classifierType = object : TypeToken<List<FontClassifier>>() {}.type
    return FontItem(
        id             = id,
        name           = name,
        slug           = slug,
        language       = language,
        description    = description,
        developer      = developer,
        fontFamily     = fontFamily,
        tags           = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        featureImageUrl = featureImageUrl,
        cardImageUrl   = cardImageUrl,
        fontFileUrl    = fontFileUrl,
        previewFileUrl = previewFileUrl,
        weightCount    = weightCount,
        categories     = gson.fromJson(categoriesJson, classifierType),
        styles         = gson.fromJson(stylesJson, classifierType),
        isPremium      = isPremium
    )
}