package com.urdufonts.app.data.repository

import com.urdufonts.app.data.local.dao.CategoryDao
import com.urdufonts.app.data.mapper.toDomain
import com.urdufonts.app.data.mapper.toEntity
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.domain.repo.CategoriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoriesRepositoryImpl(
    private val apiService: FontApiService,
    private val categoryDao: CategoryDao
) : CategoriesRepository {

    override suspend fun getCategories(): Result<List<CategoryItem>> =
        withContext(Dispatchers.IO) {
            val cached = categoryDao.getAll()
            if (cached.isNotEmpty()) return@withContext Result.success(cached.map { it.toDomain() })

            try {
                val response = apiService.getCategories()
                val categories = response.data.map { it.toDomain() }
                categoryDao.insertAll(categories.map { it.toEntity() })
                Result.success(categories)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
