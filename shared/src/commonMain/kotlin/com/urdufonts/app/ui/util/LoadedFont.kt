package com.urdufonts.app.ui.util

import androidx.compose.ui.text.font.FontFamily
import com.urdufonts.app.platform.PlatformFile

/** A downloaded .ttf/.otf font, loaded and ready to draw: Android Typeface, iOS font bytes for Skia. */
expect class LoadedFont

/** Loads a font file; throws when the file is not a valid font (corrupt or truncated download). */
expect fun loadFontFile(file: PlatformFile): LoadedFont

expect fun LoadedFont.toFontFamily(): FontFamily

/** The same font one step heavier (+300 weight where the font supports it, otherwise synthetic bold). */
expect fun LoadedFont.heavier(): LoadedFont

/** The same font in (synthetic) bold. */
expect fun LoadedFont.bold(): LoadedFont

/** Whether a font with this file name was already saved to the device's downloads by the app. */
expect fun isFontSavedInDownloads(fileName: String): Boolean
