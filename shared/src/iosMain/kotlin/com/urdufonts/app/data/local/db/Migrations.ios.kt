package com.urdufonts.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

actual val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        MIGRATION_1_2_SQL.forEach(connection::execSQL)
    }
}
