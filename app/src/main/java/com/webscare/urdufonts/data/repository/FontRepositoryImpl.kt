package com.webscare.urdufonts.data.repository

import android.content.Context
import com.webscare.urdufonts.data.local.dao.FontDao
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.mapper.toEntity
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository
import com.webscare.urdufonts.ui.util.FontPreviewCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FontRepositoryImpl(
    private val apiService: FontApiService,
    private val fontDao: FontDao,
    private val context: Context
) : FontRepository {

    override suspend fun getFonts(): List<FontItem> =
        withContext(Dispatchers.IO) {
            val cached = fontDao.getAll()
            if (cached.isNotEmpty()) return@withContext cached.map { it.toDomain() }

            val response = apiService.getFonts()
            val fonts = response.fonts.map { it.toDomain() }
            fontDao.insertAll(fonts.map { it.toEntity() })
            fonts
        }


    override suspend fun getFontById(fontId: String): FontItem? {
        return getFonts().find { it.id.toString() == fontId }
    }

    override suspend fun getFontFile(fontItem: FontItem): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cacheManager = FontPreviewCacheManager(context)
                cacheManager.getCachedFontFile(fontItem.id)?.let { return@runCatching it }
                val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")
                val response = apiService.downloadFile(url)
                if (!response.isSuccessful) throw Exception("Download failed: ${response.code()}")
                val body = response.body() ?: throw Exception("Empty response body")
                val zipFile = File(context.cacheDir, "font_${fontItem.id}_temp.zip")
                zipFile.outputStream().use { body.byteStream().copyTo(it) }
                val ttfFile = extractTtfFromZip(zipFile, fontItem.id)
                zipFile.delete() // cleanup temp zip

                ttfFile ?: throw Exception("No TTF found inside zip")
            }
        }

    private fun extractTtfFromZip(zipFile: File, fontId: Int): File? {
        val outDir = File(context.cacheDir, "font_previews").apply { mkdirs() }
        val outFile = File(outDir, "font_$fontId.ttf")

        java.util.zip.ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory &&
                    (entry.name.endsWith(".ttf", ignoreCase = true) ||
                            entry.name.endsWith(".otf", ignoreCase = true))
                ) {
                    outFile.outputStream().use { zip.copyTo(it) }
                    zip.closeEntry()

                    // Enforce last 10 rule
                    val files = outDir.listFiles()?.sortedBy { it.lastModified() }
                    if (files != null && files.size > 10) {
                        files.take(files.size - 10).forEach { it.delete() }
                    }

                    return outFile
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }
    override suspend fun getFontWeightFiles(fontItem: FontItem): Result<List<Pair<String, File>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")

                // Download zip (reuse cached zip if already there)
                val zipFile = File(context.cacheDir, "font_${fontItem.id}_temp.zip")
                if (!zipFile.exists()) {
                    val response = apiService.downloadFile(url)
                    if (!response.isSuccessful) throw Exception("Download failed: ${response.code()}")
                    val body = response.body() ?: throw Exception("Empty response body")
                    zipFile.outputStream().use { body.byteStream().copyTo(it) }
                }

                // Extract ALL ttf/otf files
                extractAllFontsFromZip(zipFile, fontItem.id)
            }
        }

    private fun extractAllFontsFromZip(zipFile: File, fontId: Int): List<Pair<String, File>> {
        val outDir = File(context.cacheDir, "font_weights_$fontId").apply { mkdirs() }
        val result = mutableListOf<Pair<String, File>>()

        java.util.zip.ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory &&
                    (entry.name.endsWith(".ttf", ignoreCase = true) ||
                            entry.name.endsWith(".otf", ignoreCase = true))
                ) {
                    // Extract clean weight name from filename
                    // e.g. "DINNextLTArabic-Bold.ttf" → "Bold"
                    val rawName = entry.name
                        .substringAfterLast("/")      // remove folder path
                        .substringAfterLast("-")       // take part after last dash
                        .substringBeforeLast(".")      // remove extension
                        .replace("_", " ")
                        .trim()

                    val outFile = File(outDir, entry.name.substringAfterLast("/"))
                    if (!outFile.exists()) {
                        outFile.outputStream().use { zip.copyTo(it) }
                    }
                    result.add(Pair(rawName, outFile))
                    zip.closeEntry()
                } else {
                    zip.closeEntry()
                }
                entry = zip.nextEntry
            }
        }

        return result.sortedBy { it.first } // sort alphabetically
    }
}