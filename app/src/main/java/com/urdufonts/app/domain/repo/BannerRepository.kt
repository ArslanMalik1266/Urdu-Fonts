package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.BannerItem

interface BannerRepository {
    fun getBanners(): List<BannerItem>
}