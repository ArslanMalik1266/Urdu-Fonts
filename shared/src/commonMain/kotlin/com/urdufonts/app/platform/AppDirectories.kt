package com.urdufonts.app.platform

/**
 * The app's private folders. Android: Context.filesDir / cacheDir (unchanged, so existing data is kept).
 * iOS: the app's Documents (kept, backed up) and Caches (can be cleared by the system) folders.
 */
expect class AppDirectories {
    /** Long-lived files: DataStore preferences, the Room database. */
    val filesDir: String

    /** Files that can be fetched again: downloaded fonts, support attachments. */
    val cacheDir: String

    /** Full path of the Room database file with this name. */
    fun databasePath(name: String): String
}
