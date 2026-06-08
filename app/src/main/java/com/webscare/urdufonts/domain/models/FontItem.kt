package com.webscare.urdufonts.domain.models

data class FontItem(
    val id: String,
    val name: String,
    val style: String,
    val category: String,
    val weightCount: Int,
    val previewImage: Int
)
