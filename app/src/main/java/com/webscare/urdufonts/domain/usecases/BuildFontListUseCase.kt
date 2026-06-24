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

            // Every 10 fonts, insert a banner
            if ((index + 1) % 10 == 0) {
                val banner = banners[bannerIndex % banners.size]
                // ✅ Unique id using bannerIndex to avoid duplicate key crash
                result.add(FontListItem.Banner(banner.copy(id = "${banner.id}_$bannerIndex")))
                bannerIndex++
            }
        }

        return result
    }
}