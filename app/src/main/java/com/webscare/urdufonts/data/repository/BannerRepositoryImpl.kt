package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.BannerItem
import com.webscare.urdufonts.domain.repo.BannerRepository

class BannerRepositoryImpl : BannerRepository {
    override fun getBanners() = listOf(
        BannerItem(id = "b1", image = R.drawable.banner_one),
        BannerItem(id = "b2", image = R.drawable.banner_two),
        BannerItem(id = "b3", image = R.drawable.banner_three),
    )
}