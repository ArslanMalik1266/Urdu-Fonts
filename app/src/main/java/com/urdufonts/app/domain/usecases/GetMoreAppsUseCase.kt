package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.MoreAppItem
import com.urdufonts.app.domain.repo.MoreAppsRepository

class GetMoreAppsUseCase(
    private val repository: MoreAppsRepository
) {
    suspend operator fun invoke(): List<MoreAppItem> = repository.getMoreApps()
}
