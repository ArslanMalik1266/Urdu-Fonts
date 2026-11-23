package com.urdufonts.app.ui.util

import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import androidx.compose.ui.text.font.FontFamily
import com.urdufonts.app.platform.PlatformFile
import java.io.File

actual typealias LoadedFont = Typeface

actual fun loadFontFile(file: PlatformFile): LoadedFont = Typeface.createFromFile(file)

actual fun LoadedFont.toFontFamily(): FontFamily = FontFamily(this)

actual fun LoadedFont.heavier(): LoadedFont =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) Typeface.create(this, (weight + 300).coerceAtMost(1000), false)
    else Typeface.create(this, Typeface.BOLD)

actual fun LoadedFont.bold(): LoadedFont = Typeface.create(this, Typeface.BOLD)

actual fun isFontSavedInDownloads(fileName: String): Boolean =
    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName).exists()
