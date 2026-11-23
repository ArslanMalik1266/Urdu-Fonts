package com.urdufonts.app.ui.fontList

import com.urdufonts.app.domain.models.FontItem

data class FontListUiState(
    val fonts: List<FontItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)