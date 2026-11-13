package com.urdufonts.app.ui.detailScreen

import com.urdufonts.app.domain.models.FontItem

enum class DetailTab(val label: String) {
    FONT("Font"),
    PREVIEW("Preview"),
    STYLES("Styles"),
    ABOUT("About"),
    INFO("Info")
}
enum class DownloadState {
    IDLE,
    DOWNLOADING,
    DOWNLOADED
}
data class FontDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fontDetail: FontItem? = null,
    val selectedTab: DetailTab = DetailTab.FONT,
    // Preview controls — visual scaffolding only for now, not yet wired to live render
    val previewFontSizePx: Float = 20f,
    val isBoldEnabled: Boolean = false,
    val isUnderlineEnabled: Boolean = false,
    val downloadState: DownloadState = DownloadState.IDLE,
    val downloadProgress: Float = 0f,
    val showDownloadLimitBottomSheet: Boolean = false,
    val downloadCount: Int = 0,
    val isProUser: Boolean = false,
    /** Fonts the user's plan allows a day; null means no limit. */
    val dailyFontLimit: Int? = com.urdufonts.app.domain.models.SubscriptionType.FREE_DAILY_FONTS,
    /** The user's plan, null on the free plan. */
    val planType: com.urdufonts.app.domain.models.SubscriptionType? = null,
    // The API sent no font_file (premium fonts, or a missing link): no live preview or weights to load
    val fontFileUnavailable: Boolean = false
)