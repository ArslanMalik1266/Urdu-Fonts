package com.urdufonts.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL

actual val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    // Used on Android: the database is opened through SupportSQLite, as before
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_1_2_SQL.forEach(db::execSQL)
    }

    // Used if a SQLite driver is ever set on the builder
    override fun migrate(connection: SQLiteConnection) {
        MIGRATION_1_2_SQL.forEach(connection::execSQL)
    }
}
