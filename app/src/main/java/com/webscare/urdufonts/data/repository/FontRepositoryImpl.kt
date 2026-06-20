package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository

class FontRepositoryImpl(
    private val apiService: FontApiService
) : FontRepository {

    override suspend fun getFonts(): List<FontItem> {
        val response = apiService.getFonts()
        println("Response: $response")
        return response.fonts.map { it.toDomain() }
    }
}