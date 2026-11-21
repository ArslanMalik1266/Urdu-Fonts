package com.urdufonts.app.domain.models

data class CategoryItem(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?
)