package com.urdufonts.app.data.repository

import com.urdufonts.app.data.mapper.toDomain
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.domain.models.MoreAppItem
import com.urdufonts.app.domain.repo.MoreAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MoreAppsRepositoryImpl(
    private val apiService: FontApiService
) : MoreAppsRepository {

    override suspend fun getMoreApps(): List<MoreAppItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMoreApps()
                if (response.status && !response.data.isNullOrEmpty()) {
                    response.data.flatMap { it.apps ?: emptyList() }.map { it.toDomain() }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
}
