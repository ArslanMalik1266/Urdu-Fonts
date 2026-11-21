package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.BannerItem
import com.urdufonts.app.domain.repo.BannerRepository

class GetBannersUseCase(
    private val repository: BannerRepository
) {
    operator fun invoke(): List<BannerItem> = repository.getBanners()
}