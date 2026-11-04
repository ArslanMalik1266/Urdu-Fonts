package com.urdufonts.app.data.mapper

import com.urdufonts.app.data.local.entity.StyleEntity
import com.urdufonts.app.data.remote.NetworkConstants
import com.urdufonts.app.data.remote.dto.StyleDto
import com.urdufonts.app.domain.models.StyleItem

private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    // Use absolute URLs as they are; only "/storage/..." style paths get the base domain
    if (startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)) return this
    return NetworkConstants.IMAGE_BASE_URL + (if (startsWith("/")) this else "/$this")
}

// ── Network DTO → Domain ──────────────────────────────────────────────────────

fun StyleDto.toDomain(): StyleItem {
    return StyleItem(
        id = id,
        title = title.orEmpty(),
        slug = slug.orEmpty(),
        description = description,
        thumbnailUrl = thumbnail.toFullImageUrl()
    )
}

// ── Domain → Room Entity ──────────────────────────────────────────────────────

fun StyleItem.toEntity(): StyleEntity = StyleEntity(
    id           = id,
    title        = title,
    slug         = slug,
    description  = description,
    thumbnailUrl = thumbnailUrl
)

// ── Room Entity → Domain ──────────────────────────────────────────────────────

fun StyleEntity.toDomain(): StyleItem = StyleItem(
    id           = id,
    title        = title,
    slug         = slug,
    description  = description,
    thumbnailUrl = thumbnailUrl
)