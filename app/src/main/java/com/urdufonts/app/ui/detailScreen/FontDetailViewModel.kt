package com.urdufonts.app.ui.detailScreen

import android.app.Activity
import android.graphics.Typeface
import android.os.Environment
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.ads.AdManager
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.usecases.GetFontDetailUseCase
import com.urdufonts.app.domain.usecases.GetFontPreviewUseCase
import com.urdufonts.app.domain.usecases.GetFontWeightsUseCase
import com.urdufonts.app.ui.util.FontDownloadManager
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FontDetailViewModel(
    private val getFontDetailUseCase: GetFontDetailUseCase,
    private val getFontPreviewUseCase: GetFontPreviewUseCase,
    private val getFontWeightsUseCase: GetFontWeightsUseCase,
    private val fontDownloadManager: FontDownloadManager,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    private val _uiState = MutableStateFlow(FontDetailUiState())
    val uiState: StateFlow<FontDetailUiState> = _uiState.asStateFlow()

    private val _fontFamilyState = MutableStateFlow<FontFamily?>(null)
    val fontFamilyState = _fontFamilyState.asStateFlow()

    private val _fontWeightsState = MutableStateFlow<List<Pair<String, FontFamily>>>(emptyList())
    val fontWeightsState = _fontWeightsState.asStateFlow()

    private val _selectedWeightIndex = MutableStateFlow(0)
    val selectedWeightIndex = _selectedWeightIndex.asStateFlow()

    private val fontId: String = checkNotNull(savedStateHandle["fontId"])
    private val _initialFontFamily = MutableStateFlow<FontFamily?>(null)
    val initialFontFamily: StateFlow<FontFamily?> = _initialFontFamily.asStateFlow()
    private var regularTypeface: Typeface? = null
    private var rawWeightTypefaces: List<Pair<String, Typeface>> = emptyList()

    init {
        loadFontDetail()
        observeDownloads()
        observeUserPreferences()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferences.isProUser.collect { isPro ->
                _uiState.update { it.copy(isProUser = isPro) }
            }
        }
        viewModelScope.launch {
            userPreferences.downloadCount.collect { count ->
                _uiState.update { it.copy(downloadCount = count) }
            }
        }
    }

    private fun observeDownloads() {
        val numericId = fontId.toIntOrNull() ?: return

        viewModelScope.launch {
            fontDownloadManager.downloadStates.collect { states ->
                val state = states[numericId] ?: DownloadState.IDLE
                _uiState.update { it.copy(downloadState = state) }
            }
        }

        viewModelScope.launch {
            fontDownloadManager.downloadProgresses.collect { progresses ->
                val progress = progresses[numericId] ?: 0f
                _uiState.update { it.copy(downloadProgress = progress) }
            }
        }

        viewModelScope.launch {
            fontDownloadManager.events.collect { event ->
                when (event) {
                    is FontDownloadManager.DownloadEvent.Success -> {
                        if (event.fontId == numericId) {
                            _eventFlow.emit(UiEvent.ShowSnackbar(event.message))
                            if (!_uiState.value.isProUser) {
                                userPreferences.incrementDownloadCount()
                            }
                        }
                    }
                    is FontDownloadManager.DownloadEvent.Failure -> {
                        if (event.fontId == numericId) {
                            _eventFlow.emit(UiEvent.ShowSnackbar(event.error))
                        }
                    }
                }
            }
        }
    }

    private fun loadFontDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                withTimeout(10_000L) {
                    getFontDetailUseCase(fontId)
                        .onSuccess { detail ->
                            _uiState.update { it.copy(isLoading = false, fontDetail = detail) }
                            checkIfAlreadyDownloaded(detail)
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
                regularTypeface = typeface
                _fontFamilyState.value = FontFamily(typeface)
            }.onFailure { e ->
                Log.e("FontDebug", "loadPreview FAILED: ${e.message}", e)
                _fontFamilyState.value = null
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
                        "Bold Regular", "Bold Italic", "BoldItalic",
                        "Light Italic", "LightItalic",
                        "Extra Light", "ExtraLight",
                        "Ultra Light", "UltraLight",
                        "Extra Bold", "ExtraBold",
                        "Ultra Bold", "UltraBold",
                        "Semi Bold", "SemiBold",
                        "Demi Bold", "DemiBold",
                        "Thin", "Light", "Regular", "Normal", "Medium",
                        "Bold", "Black", "Heavy", "Italic"
                    )

                    val stripped = file.name
                        .replace(Regex("\\[.*?\\]"), "")
                        .replace(Regex("\\.ttf$", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("\\.otf$", RegexOption.IGNORE_CASE), "")
                        .trim()

                    val weightName = knownWeights.firstOrNull { weight ->
                        stripped.contains(weight, ignoreCase = true)
                    } ?: "Regular"

                    val weightNumber = when (weightName.lowercase()) {
                        "thin"                              -> 100
                        "extra light", "extralight",
                        "ultra light", "ultralight"         -> 200
                        "light"                             -> 300
                        "light italic", "lightitalic"       -> 300
                        "regular", "normal"                 -> 400
                        "italic"                            -> 400
                        "bold regular"                      -> 400
                        "medium"                            -> 500
                        "semi bold", "semibold",
                        "demi bold", "demibold"             -> 600
                        "bold"                              -> 700
                        "bold italic", "bolditalic"         -> 700
                        "extra bold", "extrabold",
                        "ultra bold", "ultrabold"           -> 800
                        "black", "heavy"                    -> 900
                        else                                -> 400
                    }

                    val typeface = Typeface.createFromFile(file)
                    Pair(
                        "${weightName.replaceFirstChar { it.uppercase() }}  $weightNumber",
                        typeface
                    )
                }
                rawWeightTypefaces = weightFamilies
                _fontWeightsState.value = weightFamilies.map { Pair(it.first, FontFamily(it.second)) }

                val regularIndex = weightFamilies.indexOfFirst {
                    it.first.contains("Regular", ignoreCase = true)
                }
                val autoIndex = if (regularIndex >= 0) regularIndex else 0
                _selectedWeightIndex.value = autoIndex

                if (weightFamilies.isNotEmpty()) {
                    _fontFamilyState.value = FontFamily(weightFamilies[autoIndex].second)
                    if (_initialFontFamily.value == null) {
                        _initialFontFamily.value = FontFamily(weightFamilies[0].second)
                    }
                }
            }.onFailure { e ->
                Log.e("FontDebug", "loadWeights FAILED: ${e.message}", e)
            }
        }
    }

    fun onWeightSelected(index: Int) {
        _selectedWeightIndex.value = index
        val rawTf = rawWeightTypefaces.getOrNull(index)?.second ?: return
        val isBold = _uiState.value.isBoldEnabled
        val activeTf = if (isBold) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val targetWeight = (rawTf.weight + 300).coerceAtMost(1000)
                Typeface.create(rawTf, targetWeight, false)
            } else {
                Typeface.create(rawTf, Typeface.BOLD)
            }
        } else {
            rawTf
        }
        _fontFamilyState.value = FontFamily(activeTf)
    }

    fun onTabSelected(tab: DetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onPreviewFontSizeChange(size: Float) {
        _uiState.update { it.copy(previewFontSizePx = size) }
    }

    fun onBoldToggle() {
        val newBold = !_uiState.value.isBoldEnabled
        _uiState.update { it.copy(isBoldEnabled = newBold) }
        val base = regularTypeface ?: return
        val boldTf = if (newBold) Typeface.create(base, Typeface.BOLD) else base
        _initialFontFamily.value = FontFamily(boldTf)
        val currentRawTf = rawWeightTypefaces.getOrNull(_selectedWeightIndex.value)?.second ?: regularTypeface
        if (currentRawTf != null) {
            val activeTf = if (newBold) Typeface.create(currentRawTf, Typeface.BOLD) else currentRawTf
            _fontFamilyState.value = FontFamily(activeTf)
        }
    }

    fun onUnderlineToggle() {
        _uiState.update { it.copy(isUnderlineEnabled = !it.isUnderlineEnabled) }
    }

    fun retry() {
        loadFontDetail()
    }

    private fun checkIfAlreadyDownloaded(fontItem: FontItem) {
        val fileName = "${fontItem.name.replace(" ", "_")}.ttf"
        val publicFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        if (publicFile.exists()) {
            fontDownloadManager.markAsDownloaded(fontItem.id)
        }
    }

    fun downloadFont() {
        val font = _uiState.value.fontDetail ?: return
        val state = _uiState.value
        if (!state.isProUser && state.downloadCount >= 5) {
            _uiState.update { it.copy(showDownloadLimitBottomSheet = true) }
            return
        }
        fontDownloadManager.downloadFont(font)
    }

    fun dismissDownloadLimitBottomSheet() {
        _uiState.update { it.copy(showDownloadLimitBottomSheet = false) }
    }

    fun onWatchAdAndDownload(activity: Activity, adManager: AdManager) {
        dismissDownloadLimitBottomSheet()
        adManager.showRewarded(
            activity = activity,
            onRewarded = { _, _ ->
                val font = _uiState.value.fontDetail ?: return@showRewarded
                fontDownloadManager.downloadFont(font)
            },
            onNotReady = {
                val font = _uiState.value.fontDetail ?: return@showRewarded
                fontDownloadManager.downloadFont(font)
            }
        )
    }
}