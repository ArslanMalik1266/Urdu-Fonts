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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        // v2 adds fonts.isPremium. Cached rows can't know it, so drop them and let the next launch refetch.
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fonts ADD COLUMN isPremium INTEGER NOT NULL DEFAULT 0")
                db.execSQL("DELETE FROM fonts")
            }
        }
    }

    abstract fun fontDao(): FontDao
    abstract fun categoryDao(): CategoryDao
    abstract fun styleDao(): StyleDao
}