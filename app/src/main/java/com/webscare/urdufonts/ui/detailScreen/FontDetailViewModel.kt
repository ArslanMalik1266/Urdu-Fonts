package com.webscare.urdufonts.ui.detailScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.GetFontDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FontDetailViewModel(
    private val getFontDetailUseCase: GetFontDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FontDetailUiState())
    val uiState: StateFlow<FontDetailUiState> = _uiState.asStateFlow()
    private val fontId: String = checkNotNull(savedStateHandle["fontId"])

    init {
        loadFontDetail()
    }

    private fun loadFontDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getFontDetailUseCase(fontId) // Fetches specific font details
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(isLoading = false, fontDetail = detail)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load font detail"
                        )
                    }
                }
        }
    }

    fun onTabSelected(tab: DetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // Visual scaffolding only — not yet wired to live preview rendering
    fun onPreviewFontSizeChange(size: Float) {
        _uiState.update { it.copy(previewFontSizePx = size) }
    }

    fun onBoldToggle() {
        _uiState.update { it.copy(isBoldEnabled = !it.isBoldEnabled) }
    }

    fun onUnderlineToggle() {
        _uiState.update { it.copy(isUnderlineEnabled = !it.isUnderlineEnabled) }
    }

    fun retry() {
        loadFontDetail()
    }
}