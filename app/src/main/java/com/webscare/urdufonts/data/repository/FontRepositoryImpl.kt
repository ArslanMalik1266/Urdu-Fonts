package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.local.dao.FontDao
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.mapper.toEntity
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository

class FontRepositoryImpl(
    private val apiService: FontApiService,
    private val fontDao: FontDao
) : FontRepository {

    override suspend fun getFonts(): List<FontItem> {
        // Room first — return instantly if cache exists
        val cached = fontDao.getAll()
        if (cached.isNotEmpty()) return cached.map { it.toDomain() }

        // Cache is empty — fetch from network and save to Room
        val response = apiService.getFonts()
        val fonts = response.fonts.map { it.toDomain() }
        fontDao.insertAll(fonts.map { it.toEntity() })
        return fonts
    }

    override suspend fun getFontById(fontId: String): FontItem? {
        return getFonts().find { it.id.toString() == fontId }
    }
}