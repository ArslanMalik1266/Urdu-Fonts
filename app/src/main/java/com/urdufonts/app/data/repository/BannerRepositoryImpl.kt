package com.urdufonts.app.data.repository

import com.urdufonts.app.R
import com.urdufonts.app.domain.models.BannerItem
import com.urdufonts.app.domain.repo.BannerRepository

class BannerRepositoryImpl : BannerRepository {
    override fun getBanners() = listOf(
        BannerItem(id = "b1", image = R.drawable.banner_one),
        BannerItem(id = "b2", image = R.drawable.banner_two),
        BannerItem(id = "b3", image = R.drawable.banner_three),
    )
}