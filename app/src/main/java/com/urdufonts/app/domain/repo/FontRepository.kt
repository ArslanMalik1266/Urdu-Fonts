package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.FontItem
import java.io.File

interface FontRepository {
    suspend fun getFonts(): List<FontItem>
    suspend fun getFontById(fontId: String): FontItem?

    suspend fun getFontFile(fontItem: FontItem): Result<File>
    suspend fun getFontWeightFiles(fontItem: FontItem): Result<List<Pair<String, File>>>  // ← ADD
    suspend fun downloadFontToDevice(fontItem: FontItem, onProgress: (Float) -> Unit): Result<File>
}