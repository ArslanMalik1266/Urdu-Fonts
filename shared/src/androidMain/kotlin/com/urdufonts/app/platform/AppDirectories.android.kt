package com.urdufonts.app.platform

import android.content.Context

actual class AppDirectories(private val context: Context) {
    actual val filesDir: String get() = context.filesDir.absolutePath
    actual val cacheDir: String get() = context.cacheDir.absolutePath

    // Same place Room.databaseBuilder(context, ..., name) always used, so the existing database is opened
    actual fun databasePath(name: String): String = context.getDatabasePath(name).absolutePath
}
