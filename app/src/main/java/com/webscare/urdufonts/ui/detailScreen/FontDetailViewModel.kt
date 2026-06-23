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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import androidx.compose.ui.text.font.FontFamily
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
            try {
                withTimeout(10_000L) {
                    getFontDetailUseCase(fontId)
                        .onSuccess { detail ->
                            _uiState.update { it.copy(isLoading = false, fontDetail = detail) }
                            loadPreview(detail)
                            loadWeights(detail)
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = friendlyError(e))
                            }
                        }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Request timed out. Check your connection.")
                }
            } catch (e: UnknownHostException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No internet connection.")
                }
            } catch (e: SocketTimeoutException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Connection timed out. Try again.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = friendlyError(e))
                }
            }
        }
    }

    private fun friendlyError(e: Throwable): String = when (e) {
        is UnknownHostException -> "No internet connection."
        is SocketTimeoutException -> "Connection timed out. Try again."
        is TimeoutCancellationException -> "Request timed out. Check your connection."
        else -> e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong."
    }

    private fun loadPreview(fontItem: FontItem) {
        viewModelScope.launch {
            getFontPreviewUseCase(fontItem).onSuccess { file ->
                val typeface = Typeface.createFromFile(file)
                _fontFamilyState.value = FontFamily(typeface)
            }.onFailure { e ->
                Log.e("FontDebug", "loadPreview FAILED: ${e.message}", e)
                _fontFamilyState.value = null
                // Show error with retry if font file can't be loaded (e.g. offline, no cache)
                _uiState.update {
                    it.copy(errorMessage = friendlyError(e))
                }
            }
        }
    }

    private fun loadWeights(fontItem: FontItem) {
        viewModelScope.launch {
            getFontWeightsUseCase(fontItem).onSuccess { weightFiles ->
                val weightFamilies = weightFiles.map { (originalName, file) ->

                    val knownWeights = listOf(
                        // Compound weights MUST come before their components
                        "Bold Regular", "Bold Italic", "BoldItalic",
                        "Light Italic", "LightItalic",
                        "Extra Light", "ExtraLight",
                        "Ultra Light", "UltraLight",
                        "Extra Bold", "ExtraBold",
                        "Ultra Bold", "UltraBold",
                        "Semi Bold", "SemiBold",
                        "Demi Bold", "DemiBold",
                        // Simple weights after compounds
                        "Thin", "Light", "Regular", "Normal", "Medium",
                        "Bold", "Black", "Heavy", "Italic"
                    )

                    // Use file.name (actual filename) instead of originalName (which is just "[UrduFonts.com]")
                    val stripped = file.name
                        .replace(Regex("\\[.*?\\]"), "")   // remove [anything]
                        .replace(Regex("\\.ttf$", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("\\.otf$", RegexOption.IGNORE_CASE), "")
                        .trim()

                    // Find the first known weight that appears in the cleaned filename
                    val weightName = knownWeights.firstOrNull { weight ->
                        stripped.contains(weight, ignoreCase = true)
                    } ?: "Regular"

                    val typeface = Typeface.createFromFile(file)
                    Pair(weightName.replaceFirstChar { it.uppercase() }, FontFamily(typeface))
                }

                _fontWeightsState.value = weightFamilies

                // Auto-select "Regular" if exists, otherwise first
                val regularIndex = weightFamilies.indexOfFirst {
                    it.first.equals("Regular", ignoreCase = true)
                }
                val autoIndex = if (regularIndex >= 0) regularIndex else 0
                _selectedWeightIndex.value = autoIndex

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