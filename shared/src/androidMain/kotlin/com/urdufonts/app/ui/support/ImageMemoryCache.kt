package com.urdufonts.app.ui.support

import android.graphics.Bitmap
import android.util.LruCache
import java.io.File

/**
 * Decoded images kept in memory, so thumbnails and previews show at once when a list scrolls back or a
 * screen opens again. Capped at an eighth of the app's memory; the least recently used images go first.
 *
 * The key includes the file's size and last change, so a file overwritten at the same path (the convert
 * flow reuses its working files) is decoded again instead of showing the old picture.
 */
internal object ImageMemoryCache {
    private const val BYTES_PER_KB = 1024
    private const val MEMORY_SHARE = 8

    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / BYTES_PER_KB / MEMORY_SHARE).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / BYTES_PER_KB
    }

    /** Null when the file does not exist. */
    fun keyFor(path: String, maxSide: Int): String? {
        val file = File(path)
        if (!file.exists()) return null
        return "$path|${file.length()}|${file.lastModified()}|$maxSide"
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
}
