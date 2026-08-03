package com.urdufonts.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.urdufonts.app.data.local.dao.CategoryDao
import com.urdufonts.app.data.local.dao.FontDao
import com.urdufonts.app.data.local.dao.StyleDao
import com.urdufonts.app.data.local.entity.CategoryEntity
import com.urdufonts.app.data.local.entity.FontEntity
import com.urdufonts.app.data.local.entity.StyleEntity

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