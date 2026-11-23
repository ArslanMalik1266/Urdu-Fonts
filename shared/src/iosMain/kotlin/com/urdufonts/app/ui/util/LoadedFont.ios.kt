package com.urdufonts.app.ui.util

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import com.urdufonts.app.platform.AppDirectories
import com.urdufonts.app.platform.PlatformFile
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr

/**
 * The font's bytes plus the weight it should be declared with. Compose on iOS draws it through Skia.
 * Synthetic bold needs the text's own fontWeight, so [bold] only changes the declared weight here.
 */
actual class LoadedFont(val identity: String, val bytes: ByteArray, val weight: Int = 400)

actual fun loadFontFile(file: PlatformFile): LoadedFont {
    val bytes = SystemFileSystem.source(Path(file.path)).buffered().use { it.readByteArray() }
    // Fail now, like Android's Typeface.createFromFile, instead of drawing nothing later
    requireNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes))) { "Not a valid font: ${file.path}" }
    return LoadedFont(file.path, bytes)
}

actual fun LoadedFont.toFontFamily(): FontFamily =
    FontFamily(Font(identity = "$identity#$weight", data = bytes, weight = FontWeight(weight)))

actual fun LoadedFont.heavier(): LoadedFont = LoadedFont(identity, bytes, (weight + 300).coerceAtMost(900))

actual fun LoadedFont.bold(): LoadedFont = LoadedFont(identity, bytes, 700)

/** iOS saves a downloaded font into Documents/UrduFonts/<Font_Name>/ (visible in the Files app). */
actual fun isFontSavedInDownloads(fileName: String): Boolean =
    SystemFileSystem.exists(Path(AppDirectories().filesDir, "UrduFonts", fileName.substringBeforeLast('.')))
