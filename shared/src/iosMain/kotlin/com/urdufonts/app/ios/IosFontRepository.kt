package com.urdufonts.app.ios

import com.urdufonts.app.data.local.dao.FontDao
import com.urdufonts.app.data.mapper.toDomain
import com.urdufonts.app.data.mapper.toEntity
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.platform.AppDirectories
import com.urdufonts.app.platform.PlatformFile
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.write

/**
 * Fonts on iOS. Same behaviour as the Android FontRepositoryImpl: free fonts only, the list cached in Room,
 * previews extracted from the font zip into the cache (last 10 kept), and "download" saving every weight into
 * Documents/UrduFonts/<Font_Name>/, which the Files app shows (UIFileSharingEnabled in Info.plist).
 */
class IosFontRepository(
    private val apiService: FontApiService,
    private val fontDao: FontDao,
    private val directories: AppDirectories,
) : FontRepository {

    private var memoryFonts: List<FontItem>? = null

    override suspend fun getFonts(): List<FontItem> {
        memoryFonts?.let { return it }
        return withContext(Dispatchers.IO) {
            val cached = fontDao.getAll()
            val all = if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else apiService.getFonts().fonts.map { it.toDomain() }.also { fonts -> fontDao.insertAll(fonts.map { it.toEntity() }) }
            all.filterNot { it.isPremium }.also { memoryFonts = it }
        }
    }

    override suspend fun getFontById(fontId: String): FontItem? = getFonts().find { it.id.toString() == fontId }

    override suspend fun getFontFile(fontItem: FontItem): Result<PlatformFile> = withContext(Dispatchers.IO) {
        runCatching {
            val previews = folder(directories.cacheDir, "font_previews")
            val target = Path(previews, "font_${fontItem.id}.ttf")
            if (SystemFileSystem.exists(target)) return@runCatching PlatformFile(target.toString())
            val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")
            val zip = download(url, Path(directories.cacheDir, "font_${fontItem.id}_preview.zip")) { }
            val entry = ZipReader.entries(zip).firstOrNull { !it.isDirectory && it.isFontFile() }
                ?: throw Exception("No TTF found inside zip")
            writeAtomically(target, entry.read())
            keepNewest(previews, 10)
            PlatformFile(target.toString())
        }
    }

    override suspend fun getFontWeightFiles(fontItem: FontItem): Result<List<Pair<String, PlatformFile>>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")
            val zipPath = Path(directories.cacheDir, "font_${fontItem.id}_weights.zip")
            val zip = if (SystemFileSystem.exists(zipPath)) readAll(zipPath) else download(url, zipPath) { }
            extractFonts(zip, folder(directories.cacheDir, "font_weights_${fontItem.id}"))
        }
    }

    override suspend fun downloadFontToDevice(fontItem: FontItem, onProgress: (Float) -> Unit): Result<PlatformFile> = withContext(Dispatchers.IO) {
        runCatching {
            val url = fontItem.fontFileUrl ?: throw IllegalArgumentException("URL is null")
            val zip = download(url, Path(directories.cacheDir, "font_${fontItem.id}_download.zip"), onProgress)
            // Saved where the user can reach it: Files app → On My iPhone → Urdu Fonts → UrduFonts
            val target = folder(directories.filesDir, "UrduFonts", fontItem.name.replace(" ", "_"))
            val saved = extractFonts(zip, target)
            if (saved.isEmpty()) throw Exception("No font files found inside ZIP")
            saved.first().second
        }
    }

    // ── Files ──────────────────────────────────────────────────────────────

    private fun ZipReader.Entry.isFontFile() = name.endsWith(".ttf", true) || name.endsWith(".otf", true)

    /** Every .ttf/.otf in the zip, named by the part after the last dash ("DINNext-Bold.ttf" → "Bold"). */
    private fun extractFonts(zip: ByteArray, outDir: Path): List<Pair<String, PlatformFile>> =
        ZipReader.entries(zip).filter { !it.isDirectory && it.isFontFile() }.map { entry ->
            val fileName = entry.name.substringAfterLast('/')
            val weightName = fileName.substringAfterLast('-').substringBeforeLast('.').replace('_', ' ').trim()
            val out = Path(outDir, fileName)
            if (!SystemFileSystem.exists(out)) writeAtomically(out, entry.read())
            weightName to PlatformFile(out.toString())
        }.sortedBy { it.first }

    /** Downloads [url] to [target] (through a ".part" file) and returns its bytes. */
    private suspend fun download(url: String, target: Path, onProgress: (Float) -> Unit): ByteArray {
        val bytes = apiService.downloadFile(url) { body, length ->
            val buffer = Buffer()
            val chunk = ByteArray(8192)
            var read = 0L
            while (true) {
                val n = body.readAvailable(chunk, 0, chunk.size)
                if (n < 0) break
                if (n == 0) continue
                buffer.write(chunk, 0, n)
                read += n
                if (length != null && length > 0) onProgress(read.toFloat() / length)
            }
            buffer.readByteArray()
        }
        writeAtomically(target, bytes)
        return bytes
    }

    private fun readAll(path: Path): ByteArray = SystemFileSystem.source(path).buffered().use { it.readByteArray() }

    /** Writes to "<name>.part" and renames, so a half-written file is never taken as complete. */
    private fun writeAtomically(target: Path, bytes: ByteArray) {
        val part = Path("$target.part")
        SystemFileSystem.sink(part).buffered().use { it.write(bytes) }
        SystemFileSystem.delete(target, mustExist = false)
        SystemFileSystem.atomicMove(part, target)
    }

    private fun folder(base: String, vararg parts: String): Path =
        Path(base, *parts).also { SystemFileSystem.createDirectories(it) }

    /** Keeps the [keep] most recently written files in [dir]. kotlinx-io has no file dates, so the names' order stands in. */
    private fun keepNewest(dir: Path, keep: Int) {
        val files = SystemFileSystem.list(dir).filter { !it.name.endsWith(".part") }
        if (files.size <= keep) return
        files.sortedBy { modifiedSeconds(it) }.take(files.size - keep).forEach { SystemFileSystem.delete(it, mustExist = false) }
    }

    private fun modifiedSeconds(path: Path): Double {
        val attrs = platform.Foundation.NSFileManager.defaultManager.attributesOfItemAtPath(path.toString(), null)
        val date = attrs?.get(platform.Foundation.NSFileModificationDate) as? platform.Foundation.NSDate
        return date?.timeIntervalSince1970 ?: 0.0
    }
}
