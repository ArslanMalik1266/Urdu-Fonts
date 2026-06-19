package com.webscare.urdufonts.ui.fontList

import com.webscare.urdufonts.domain.models.FontItem

data class FontListUiState(
    val fonts: List<FontItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)