package com.webscare.urdufonts.ui.util

import android.content.Context
import java.io.File
import java.io.InputStream

class FontPreviewCacheManager(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "font_previews").apply { if (!exists()) mkdirs() }

    fun getCachedFontFile(fontId: Int): File? {
        val file = File(cacheDir, "font_$fontId.ttf")
        return if (file.exists()) file else null
    }

    fun saveFontFile(fontId: Int, inputStream: InputStream) {
        val file = File(cacheDir, "font_$fontId.ttf")
        file.outputStream().use { output -> inputStream.copyTo(output) }

        // Enforce the "Last 10" rule
        val files = cacheDir.listFiles()?.sortedBy { it.lastModified() }
        if (files != null && files.size > 10) {
            files.take(files.size - 10).forEach { it.delete() }
        }
    }
}