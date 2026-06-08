package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.BannerItem
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.models.FontListItem

class BuildFontListUseCase {

    operator fun invoke(
        fonts: List<FontItem>,
        banners: List<BannerItem>
    ): List<FontListItem> {
        if (banners.isEmpty()) return fonts.map { FontListItem.Font(it) }

        val result = mutableListOf<FontListItem>()
        var bannerIndex = 0

        fonts.forEachIndexed { index, font ->
            result.add(FontListItem.Font(font))

            val isIntervalHit = (index + 1) % 7 == 0
            val hasMoreBanners = fonts.size > 26|| bannerIndex < banners.size

            if (isIntervalHit && hasMoreBanners) {
                result.add(FontListItem.Banner(banners[bannerIndex % banners.size]))
                bannerIndex++
            }
        }

        return result
    }
}