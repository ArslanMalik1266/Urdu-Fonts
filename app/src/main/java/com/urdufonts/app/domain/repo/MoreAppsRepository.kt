package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.MoreAppItem

interface MoreAppsRepository {
    suspend fun getMoreApps(): List<MoreAppItem>
}
