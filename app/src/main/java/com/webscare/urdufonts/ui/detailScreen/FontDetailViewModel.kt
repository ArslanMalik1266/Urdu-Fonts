package com.webscare.urdufonts.ui.detailScreen

import android.graphics.Typeface
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.usecases.GetFontDetailUseCase
import com.webscare.urdufonts.domain.usecases.GetFontPreviewUseCase
import com.webscare.urdufonts.domain.usecases.GetFontWeightsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily

class FontDetailViewModel(
    private val getFontDetailUseCase: GetFontDetailUseCase,
    private val getFontPreviewUseCase: GetFontPreviewUseCase,
    private val getFontWeightsUseCase: GetFontWeightsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FontDetailUiState())
    val uiState: StateFlow<FontDetailUiState> = _uiState.asStateFlow()

    private val _fontFamilyState = MutableStateFlow<FontFamily?>(null)
    val fontFamilyState = _fontFamilyState.asStateFlow()

    private val _fontWeightsState = MutableStateFlow<List<Pair<String, FontFamily>>>(emptyList())
    val fontWeightsState = _fontWeightsState.asStateFlow()

    private val _selectedWeightIndex = MutableStateFlow(0)
    val selectedWeightIndex = _selectedWeightIndex.asStateFlow()

    private val fontId: String = checkNotNull(savedStateHandle["fontId"])

    init {
        loadFontDetail()
    }

    private fun loadFontDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getFontDetailUseCase(fontId)
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(isLoading = false, fontDetail = detail)
                    }
                    loadPreview(detail)
                    loadWeights(detail)
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

    private fun loadPreview(fontItem: FontItem) {
        viewModelScope.launch {
            Log.d("FontDebug", "loadPreview called for: ${fontItem.name}, url: ${fontItem.fontFileUrl}")
            getFontPreviewUseCase(fontItem).onSuccess { file ->
                Log.d("FontDebug", "File downloaded: ${file.absolutePath}, exists: ${file.exists()}, size: ${file.length()}")
                val typeface = Typeface.createFromFile(file)
                _fontFamilyState.value = FontFamily(typeface)
                Log.d("FontDebug", "FontFamily set successfully")
            }.onFailure { e ->
                Log.e("FontDebug", "loadPreview FAILED: ${e.message}", e)
                _fontFamilyState.value = null
            }
        }
    }

    private fun loadWeights(fontItem: FontItem) {
        viewModelScope.launch {
            getFontWeightsUseCase(fontItem).onSuccess { weightFiles ->
                Log.d("FontDebug", "Weights found: ${weightFiles.size} → ${weightFiles.map { it.first }}")
                val weightFamilies = weightFiles.map { (name, file) ->
                    val typeface = Typeface.createFromFile(file)
                    Pair(name, FontFamily(typeface))
                }
                _fontWeightsState.value = weightFamilies

                // Auto-select Regular if exists, otherwise first
                val regularIndex = weightFamilies.indexOfFirst {
                    it.first.equals("Regular", ignoreCase = true)
                }
                val autoIndex = if (regularIndex >= 0) regularIndex else 0
                _selectedWeightIndex.value = autoIndex

                // Update preview font to the auto-selected weight
                if (weightFamilies.isNotEmpty()) {
                    _fontFamilyState.value = weightFamilies[autoIndex].second
                }
            }.onFailure { e ->
                Log.e("FontDebug", "loadWeights FAILED: ${e.message}", e)
            }
        }
    }

    fun onWeightSelected(index: Int) {
        _selectedWeightIndex.value = index
        val weights = _fontWeightsState.value
        if (index < weights.size) {
            _fontFamilyState.value = weights[index].second
        }
    }

    fun onTabSelected(tab: DetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

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