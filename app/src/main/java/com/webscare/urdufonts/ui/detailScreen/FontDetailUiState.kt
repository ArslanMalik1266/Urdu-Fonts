package com.webscare.urdufonts.ui.detailScreen

import com.webscare.urdufonts.domain.models.FontItem

enum class DetailTab(val label: String) {
    FONT("Font"),
    PREVIEW("Preview"),
    STYLES("Styles"),
    ABOUT("About"),
    INFO("Info")
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
)