package com.webscare.urdufonts.domain.models

data class StyleItem(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?
)