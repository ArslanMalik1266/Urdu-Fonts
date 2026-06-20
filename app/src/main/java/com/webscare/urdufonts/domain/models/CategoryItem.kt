package com.webscare.urdufonts.domain.models

data class CategoryItem(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?
)