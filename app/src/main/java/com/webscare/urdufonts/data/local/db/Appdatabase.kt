package com.webscare.urdufonts.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.webscare.urdufonts.data.local.dao.CategoryDao
import com.webscare.urdufonts.data.local.dao.FontDao
import com.webscare.urdufonts.data.local.dao.StyleDao
import com.webscare.urdufonts.data.local.entity.CategoryEntity
import com.webscare.urdufonts.data.local.entity.FontEntity
import com.webscare.urdufonts.data.local.entity.StyleEntity

@Database(
    entities = [
        FontEntity::class,
        CategoryEntity::class,
        StyleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fontDao(): FontDao
    abstract fun categoryDao(): CategoryDao
    abstract fun styleDao(): StyleDao
}