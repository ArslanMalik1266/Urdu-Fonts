package com.urdufonts.app.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSUserDomainMask

actual class AppDirectories {
    actual val filesDir: String get() = directory(NSDocumentDirectory)
    actual val cacheDir: String get() = directory(NSCachesDirectory)
    actual fun databasePath(name: String): String = "$filesDir/$name"

    @OptIn(ExperimentalForeignApi::class)
    private fun directory(which: NSSearchPathDirectory): String {
        val url = NSFileManager.defaultManager.URLForDirectory(
            directory = which,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        return requireNotNull(url?.path) { "No app directory for $which" }
    }
}
