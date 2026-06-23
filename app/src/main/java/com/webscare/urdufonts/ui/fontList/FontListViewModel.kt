package com.webscare.urdufonts.ui.fontList

import androidx.lifecycle.ViewModel
import com.webscare.urdufonts.domain.models.FontItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import kotlinx.coroutines.launch

class FontListViewModel(
    private val getFontsUseCase: GetFontsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve parameters passed via route
    private val filterType: String? = savedStateHandle["filterType"]
    private val filterValue: String? = savedStateHandle["filterValue"]

    private val _uiState = MutableStateFlow(FontListUiState())
    val uiState = _uiState.asStateFlow()

    private var allFilteredFonts: List<FontItem> = emptyList()

    init {
        loadFonts()
    }

    private fun loadFonts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch all fonts
                val rawFonts = getFontsUseCase()

                // Filter by category or style using classifier slugs
                val filtered = when (filterType) {
                    "category" -> rawFonts.filter { font ->
                        font.categories?.any { it.slug == filterValue } == true  // ← slug not id
                    }
                    "style" -> rawFonts.filter { font ->
                        font.styles?.any { it.slug == filterValue } == true      // ← slug not id
                    }
                    else -> rawFonts
                }

                allFilteredFonts = filtered

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fonts = filtered
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load fonts list"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            // Filter local list for search queries
            val searched = if (query.isBlank()) {
                allFilteredFonts
            } else {
                allFilteredFonts.filter { it.name.contains(query, ignoreCase = true) }
            }
            current.copy(searchQuery = query, fonts = searched)
        }
    }

    fun retry() {
        loadFonts()
    }
    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", fonts = allFilteredFonts) }
    }

}