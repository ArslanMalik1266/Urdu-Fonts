package com.webscare.urdufonts.data.mapper

import com.webscare.urdufonts.data.local.entity.StyleEntity
import com.webscare.urdufonts.data.remote.NetworkConstants
import com.webscare.urdufonts.data.remote.dto.StyleDto
import com.webscare.urdufonts.domain.models.StyleItem

private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return NetworkConstants.IMAGE_BASE_URL + this
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