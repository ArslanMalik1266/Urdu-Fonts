package com.urdufonts.app.`data`.local.db

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  actual override fun initialize(): AppDatabase = com.urdufonts.app.`data`.local.db.AppDatabase_Impl()
}
