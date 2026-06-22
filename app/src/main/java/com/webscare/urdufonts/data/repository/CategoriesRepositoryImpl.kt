package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.local.dao.CategoryDao
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.mapper.toEntity
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.domain.repo.CategoriesRepository

class CategoriesRepositoryImpl(
    private val apiService: FontApiService,
    private val categoryDao: CategoryDao
) : CategoriesRepository {

    override suspend fun getCategories(): Result<List<CategoryItem>> {
        // Room first — return instantly if cache exists
        val cached = categoryDao.getAll()
        if (cached.isNotEmpty()) return Result.success(cached.map { it.toDomain() })

        // Cache is empty — fetch from network and save to Room
        return try {
            val response = apiService.getCategories()
            val categories = response.data.map { it.toDomain() }
            categoryDao.insertAll(categories.map { it.toEntity() })
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}