package com.urdufonts.app.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
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
    version = 2,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fontDao(): FontDao
    abstract fun categoryDao(): CategoryDao
    abstract fun styleDao(): StyleDao

    companion object {
        const val FILE_NAME = "urdufonts.db"
    }
}

/** Room's KSP processor writes the actual of this for every platform. */
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** SQL for v1 → v2: adds fonts.isPremium. Cached rows can't know it, so they are dropped and refetched. */
internal val MIGRATION_1_2_SQL = listOf(
    "ALTER TABLE fonts ADD COLUMN isPremium INTEGER NOT NULL DEFAULT 0",
    "DELETE FROM fonts"
)

/**
 * v1 → v2. Platform-specific: Android keeps opening the existing database through SupportSQLite,
 * iOS uses the bundled SQLite driver, and each needs its own migrate() override.
 */
expect val MIGRATION_1_2: Migration
