package com.webscare.urdufonts.ui.home

import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.models.FontListItem

data class HomeUiState(
    val allFonts: List<FontListItem> = emptyList(),
    val fonts: List<FontListItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)