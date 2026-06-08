package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.BannerItem

interface BannerRepository {
    fun getBanners(): List<BannerItem>
}