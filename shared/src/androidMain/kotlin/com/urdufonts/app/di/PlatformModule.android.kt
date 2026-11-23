package com.urdufonts.app.di

import android.content.Context
import androidx.room.Room
import com.urdufonts.app.data.local.db.AppDatabase
import com.urdufonts.app.data.local.db.MIGRATION_1_2
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import com.urdufonts.app.platform.AndroidDeviceDetailsSource
import com.urdufonts.app.platform.AppDirectories
import org.koin.dsl.module

actual val platformModule = module {
    single { AppDirectories(get<Context>()) }

    // No SQLite driver set: Android keeps its built-in SQLite, and the existing urdufonts.db is opened as before
    single<AppDatabase> {
        val context = get<Context>()
        Room.databaseBuilder<AppDatabase>(context, get<AppDirectories>().databasePath(AppDatabase.FILE_NAME))
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single<DeviceDetailsSource> { AndroidDeviceDetailsSource() }
    single { com.urdufonts.app.ui.platform.ExternalNavigator(get<Context>()) }
}
