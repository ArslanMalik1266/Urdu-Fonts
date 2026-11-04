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

    // Kept for the app session so every screen gets the same list instantly after the first fetch
    @Volatile private var cachedApps: List<MoreAppItem>? = null

    override suspend fun getMoreApps(): List<MoreAppItem> {
        cachedApps?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMoreApps()
                if (response.status && !response.data.isNullOrEmpty()) {
                    response.data.flatMap { it.apps ?: emptyList() }.map { it.toDomain() }
                        .also { if (it.isNotEmpty()) cachedApps = it }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
