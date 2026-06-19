package com.webscare.urdufonts.ui.fontList

import androidx.lifecycle.ViewModel
import com.webscare.urdufonts.domain.models.FontItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FontListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FontListUiState())
    val uiState = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDownloadClick(fontItem: FontItem) {
        // handle download
    }
}