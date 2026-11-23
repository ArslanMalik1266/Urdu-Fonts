package com.urdufonts.app.data.mapper

import com.urdufonts.app.data.local.entity.CategoryEntity
import com.urdufonts.app.data.remote.NetworkConstants
import com.urdufonts.app.data.remote.dto.CategoryDto
import com.urdufonts.app.domain.models.CategoryItem

private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    // Use absolute URLs as they are; only "/storage/..." style paths get the base domain
    if (startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)) return this
    return NetworkConstants.IMAGE_BASE_URL + (if (startsWith("/")) this else "/$this")
}

// ── Network DTO → Domain ──────────────────────────────────────────────────────

fun CategoryDto.toDomain(): CategoryItem {
    return CategoryItem(
        id = id,
        title = title.orEmpty(),
        slug = slug.orEmpty(),
        description = description,
        thumbnailUrl = thumbnail.toFullImageUrl()
    )
}

// ── Domain → Room Entity ──────────────────────────────────────────────────────

fun CategoryItem.toEntity(): CategoryEntity = CategoryEntity(
    id           = id,
    title        = title,
    slug         = slug,
    description  = description,
    thumbnailUrl = thumbnailUrl
)

// ── Room Entity → Domain ──────────────────────────────────────────────────────

fun CategoryEntity.toDomain(): CategoryItem = CategoryItem(
    id           = id,
    title        = title,
    slug         = slug,
    description  = description,
    thumbnailUrl = thumbnailUrl
)