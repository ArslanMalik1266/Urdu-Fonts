package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.BannerItem
import com.webscare.urdufonts.domain.repo.BannerRepository

class GetBannersUseCase(
    private val repository: BannerRepository
) {
    operator fun invoke(): List<BannerItem> = repository.getBanners()
}