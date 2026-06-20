package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.domain.repo.StylesRepository

class StylesRepositoryImpl(
    private val apiService: FontApiService
) : StylesRepository {

    override suspend fun getStyles(): Result<List<StyleItem>> {
        return try {
            val response = apiService.getStyles()
            println("StylesResponse: $response")
            val styles = response.data.map { it.toDomain() }
            println("StylesResponse: $styles")
            Result.success(styles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}