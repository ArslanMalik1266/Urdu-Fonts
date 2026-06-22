package com.webscare.urdufonts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fonts")
data class FontEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val slug: String,
    val language: String,
    val description: String,
    val developer: String,
    val fontFamily: String,
    val tags: String,               // stored as comma-separated string
    val featureImageUrl: String?,
    val cardImageUrl: String?,
    val fontFileUrl: String?,
    val previewFileUrl: String?,
    val weightCount: String,
    val categoriesJson: String,     // stored as JSON string
    val stylesJson: String          // stored as JSON string
)
