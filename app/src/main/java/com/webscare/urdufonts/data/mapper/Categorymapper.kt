package com.webscare.urdufonts.data.mapper

import com.webscare.urdufonts.data.remote.NetworkConstants
import com.webscare.urdufonts.data.remote.dto.CategoryDto
import com.webscare.urdufonts.domain.models.CategoryItem

private fun String?.toFullImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return NetworkConstants.IMAGE_BASE_URL + this
}

fun CategoryDto.toDomain(): CategoryItem {
    return CategoryItem(
        id = id,
        title = title.orEmpty(),
        slug = slug.orEmpty(),
        description = description,
        thumbnailUrl = thumbnail.toFullImageUrl()
    )
}