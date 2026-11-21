package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.platform.PlatformFile

interface FontRepository {
    suspend fun getFonts(): List<FontItem>
    suspend fun getFontById(fontId: String): FontItem?

    suspend fun getFontFile(fontItem: FontItem): Result<PlatformFile>
    suspend fun getFontWeightFiles(fontItem: FontItem): Result<List<Pair<String, PlatformFile>>>  // ← ADD
    suspend fun downloadFontToDevice(fontItem: FontItem, onProgress: (Float) -> Unit): Result<PlatformFile>
}