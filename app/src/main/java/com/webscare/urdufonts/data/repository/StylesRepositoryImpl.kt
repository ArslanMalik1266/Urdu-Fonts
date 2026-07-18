package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.local.dao.StyleDao
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.data.mapper.toEntity
import com.webscare.urdufonts.data.remote.api.FontApiService
import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.domain.repo.StylesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StylesRepositoryImpl(
    private val apiService: FontApiService,
    private val styleDao: StyleDao
) : StylesRepository {

    override suspend fun getStyles(): Result<List<StyleItem>> =
        withContext(Dispatchers.IO) {
            val cached = styleDao.getAll()
            if (cached.isNotEmpty()) return@withContext Result.success(cached.map { it.toDomain() })

            try {
                val response = apiService.getStyles()
                val styles = response.data.map { it.toDomain() }
                styleDao.insertAll(styles.map { it.toEntity() })
                Result.success(styles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
