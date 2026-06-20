package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.domain.repo.CategoriesRepository

class CategoriesRepositoryImpl(
    private val apiService: FontApiService
) : CategoriesRepository {

    override suspend fun getCategories(): Result<List<CategoryItem>> {
        return try {
            val response = apiService.getCategories()
            val categories = response.data.map { it.toDomain() }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}