package com.webscare.urdufonts.domain.models

sealed class FontListItem {
    data class Font(val fontItem: FontItem) : FontListItem()
    data class Banner(val bannerItem: BannerItem) : FontListItem()
}