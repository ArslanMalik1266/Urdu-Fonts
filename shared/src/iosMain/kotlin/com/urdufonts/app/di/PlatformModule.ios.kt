package com.urdufonts.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.urdufonts.app.data.local.db.AppDatabase
import com.urdufonts.app.data.local.db.MIGRATION_1_2
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import com.urdufonts.app.platform.AppDirectories
import com.urdufonts.app.platform.IosDeviceDetailsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

actual val platformModule = module {
    single { AppDirectories() }
    // Coil's context, which some ViewModels use to preload images (Android gets it from androidContext())
    single<coil3.PlatformContext> { coil3.PlatformContext.INSTANCE }

    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(name = get<AppDirectories>().databasePath(AppDatabase.FILE_NAME))
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single<DeviceDetailsSource> { IosDeviceDetailsSource() }
    single { com.urdufonts.app.ui.platform.ExternalNavigator() }
}
