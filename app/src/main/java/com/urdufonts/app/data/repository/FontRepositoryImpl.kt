package com.urdufonts.app.data.repository

import android.content.Context
import com.urdufonts.app.data.local.dao.FontDao
import com.urdufonts.app.data.mapper.toDomain
import com.urdufonts.app.data.mapper.toEntity
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.ui.util.FontPreviewCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.FileOutputStream

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


    override suspend fun downloadFontToDevice(
        fontItem: FontItem,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")
            val response = apiService.downloadFile(url)
            if (!response.isSuccessful) throw Exception("Download failed: ${response.code()}")

            val body = response.body() ?: throw Exception("Empty response body")
            val totalBytes = body.contentLength()

            // 1. Download ZIP to cache temp directory first
            val tempZipFile = File(context.cacheDir, "font_${fontItem.id}_temp.zip")
            body.byteStream().use { inputStream ->
                tempZipFile.outputStream().use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0
                    var read = inputStream.read(buffer)
                    while (read != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesRead += read
                        if (totalBytes > 0) {
                            onProgress(bytesRead.toFloat() / totalBytes)
                        }
                        read = inputStream.read(buffer)
                    }
                }
            }

            // 2. Extract ALL font weights from the ZIP locally
            val extractedWeights = extractAllFontsFromZip(tempZipFile, fontItem.id)
            tempZipFile.delete() // Clean up ZIP immediately

            if (extractedWeights.isEmpty()) throw Exception("No font files found inside ZIP")

            // Subfolder path: UrduFonts/FontName (e.g. UrduFonts/Jameel_Noori)
            val cleanSubfolderName = "UrduFonts/${fontItem.name.replace(" ", "_")}"

            // 3. Save each extracted weight into the public subfolder safely
            extractedWeights.forEach { (_, weightFile) ->
                saveExtractedWeightFile(weightFile, cleanSubfolderName)
            }

            // Clean up temporary local weight cache directory
            val localCacheDir = File(context.cacheDir, "font_weights_${fontItem.id}")
            localCacheDir.deleteRecursively()

            // Return a reference to the first extracted file
            extractedWeights.first().second
        }
    }

    private fun saveExtractedWeightFile(
        weightFile: File,
        cleanSubfolderName: String
    ) {
        val fileName = weightFile.name
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$cleanSubfolderName/"
            try {
                // Clean up any stale MediaStore entries with same filename in this relative path
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, relativePath)
                resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs)
            } catch (_: Exception) {
                // Ignore and proceed
            }

            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "font/ttf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri, "wt")?.use { outputStream ->
                        weightFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return
                }
            } catch (_: Exception) {
                // Fallback to direct file writing if MediaStore insert fails
                saveViaDirectFile(weightFile, cleanSubfolderName)
                return
            }
            saveViaDirectFile(weightFile, cleanSubfolderName)
        } else {
            saveViaDirectFile(weightFile, cleanSubfolderName)
        }
    }

    private fun saveViaDirectFile(weightFile: File, cleanSubfolderName: String) {
        try {
            val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetSubfolder = File(publicDownloadsDir, cleanSubfolderName).apply { mkdirs() }
            val targetFile = File(targetSubfolder, weightFile.name)

            weightFile.inputStream().use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf("font/ttf"),
                null
            )
        } catch (_: Exception) {
            // Safe fallback completed
        }
    }




}