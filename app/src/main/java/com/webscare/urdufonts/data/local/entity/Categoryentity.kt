package com.webscare.urdufonts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?
)