package com.urdufonts.app.ui.detailScreen

import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.ui.util.LoadedFont
import com.urdufonts.app.ui.util.bold
import com.urdufonts.app.ui.util.heavier
import com.urdufonts.app.ui.util.isFontSavedInDownloads
import com.urdufonts.app.ui.util.loadFontFile
import com.urdufonts.app.ui.util.toFontFamily
import com.urdufonts.app.platform.AppLog as Log
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
import com.urdufonts.app.ui.util.UserMessages
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.urdufonts.app.platform.absolutePathString

class FontDetailViewModel(
    private val getFontDetailUseCase: GetFontDetailUseCase,
    private val getFontPreviewUseCase: GetFontPreviewUseCase,
    private val getFontWeightsUseCase: GetFontWeightsUseCase,
    private val fontDownloadManager: FontDownloadManager,
    private val userPreferences: UserPreferences,
    private val entitlementRepository: com.urdufonts.app.domain.repo.EntitlementRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        // Tracks unique font IDs downloaded in the current app session (resets on app restart)
        private val sessionDownloadedFontIds = mutableSetOf<String>()
    }

    sealed class UiEvent {
        data class DownloadSuccess(val message: String, val shouldShowAd: Boolean = false) : UiEvent()
        data class DownloadFailure(val error: String) : UiEvent()
        data class ShowSnackbar(val message: String, val shouldShowAd: Boolean = false) : UiEvent()
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
    private var regularTypeface: LoadedFont? = null
    private var rawWeightTypefaces: List<Pair<String, LoadedFont>> = emptyList()

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
            // The daily font limit follows the plan: Free 10, Basic 50, Pro 100, Business 200
            entitlementRepository.entitlementsFlow.collect { entitlements ->
                _uiState.update { it.copy(dailyFontLimit = entitlements.dailyFontLimit, planType = entitlements.activeSubscriptionType) }
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
                            val currentIdStr = numericId.toString()
                            val isFirstDownloadInSession = sessionDownloadedFontIds.add(currentIdStr)
                            val shouldShowAd = isFirstDownloadInSession && !_uiState.value.isProUser
                            _eventFlow.emit(UiEvent.DownloadSuccess(message = event.message, shouldShowAd = shouldShowAd))
                            // Counted on every plan with a daily limit
                            if (_uiState.value.dailyFontLimit != null) {
                                userPreferences.recordFontDownloaded(currentIdStr)
                            }
                        }
                    }
                    is FontDownloadManager.DownloadEvent.Failure -> {
                        if (event.fontId == numericId) {
                            _eventFlow.emit(UiEvent.DownloadFailure(error = event.error))
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
                            // No font_file (premium, or a missing link): show the page with the preview image
                            // instead of trying to download a null URL
                            val fileUnavailable = detail.fontFileUrl.isNullOrBlank()
                            _uiState.update {
                                it.copy(isLoading = false, fontDetail = detail, fontFileUnavailable = fileUnavailable)
                            }
                            checkIfAlreadyDownloaded(detail)
                            if (!fileUnavailable) {
                                loadPreview(detail)
                                loadWeights(detail)
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = friendlyError(e))
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = friendlyError(e))
                }
            }
        }
    }

    // Raw exception text is logged only; users get UserMessages
    private fun friendlyError(e: Throwable, fallback: String = UserMessages.GENERIC): String {
        Log.e("FontDebug", "FontDetail error: ${e.message}", e)
        return UserMessages.forError(e, fallback)
    }

    private fun loadPreview(fontItem: FontItem) {
        viewModelScope.launch {
            getFontPreviewUseCase(fontItem).onSuccess { file ->
                // A corrupt or truncated font file throws from native parsing; skip the preview instead of crashing
                val typeface = runCatching { loadFontFile(file) }
                    .onFailure { Log.e("FontDebug", "loadPreview bad font file: ${file.absolutePathString.substringAfterLast('/')}", it) }
                    .getOrNull() ?: return@onSuccess
                regularTypeface = typeface
                _fontFamilyState.value = typeface.toFontFamily()
            }.onFailure { e ->
                Log.e("FontDebug", "loadPreview FAILED: ${e.message}", e)
                _fontFamilyState.value = null
                _uiState.update {
                    it.copy(errorMessage = friendlyError(e, UserMessages.FONT_FILE_FAILED))
                }
            }
        }
    }

    private fun loadWeights(fontItem: FontItem) {
        viewModelScope.launch {
            getFontWeightsUseCase(fontItem).onSuccess { weightFiles ->
                val weightFamilies = weightFiles.mapNotNull { (originalName, file) ->
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

                    val stripped = file.absolutePathString.substringAfterLast('/')
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

                    // Skip weight files that fail to parse rather than crashing the whole screen
                    val typeface = runCatching { loadFontFile(file) }
                        .onFailure { Log.e("FontDebug", "loadWeights bad font file: ${file.absolutePathString.substringAfterLast('/')}", it) }
                        .getOrNull() ?: return@mapNotNull null
                    Pair(
                        "${weightName.replaceFirstChar { it.uppercase() }}  $weightNumber",
                        typeface
                    )
                }
                rawWeightTypefaces = weightFamilies
                _fontWeightsState.value = weightFamilies.map { Pair(it.first, it.second.toFontFamily()) }

                val regularIndex = weightFamilies.indexOfFirst {
                    it.first.contains("Regular", ignoreCase = true)
                }
                val autoIndex = if (regularIndex >= 0) regularIndex else 0
                _selectedWeightIndex.value = autoIndex

                if (weightFamilies.isNotEmpty()) {
                    _fontFamilyState.value = weightFamilies[autoIndex].second.toFontFamily()
                    if (_initialFontFamily.value == null) {
                        _initialFontFamily.value = weightFamilies[0].second.toFontFamily()
                    }
                } else {
                    // Every weight file failed to parse: show the error state instead of loading forever
                    _uiState.update { it.copy(errorMessage = UserMessages.FONT_FILE_FAILED) }
                }
            }.onFailure { e ->
                // Without weights the screen would stay on its skeleton forever
                _uiState.update { it.copy(errorMessage = friendlyError(e, UserMessages.FONT_FILE_FAILED)) }
            }
        }
    }

    fun onWeightSelected(index: Int) {
        _selectedWeightIndex.value = index
        val rawTf = rawWeightTypefaces.getOrNull(index)?.second ?: return
        val isBold = _uiState.value.isBoldEnabled
        val activeTf = if (isBold) rawTf.heavier() else rawTf
        _fontFamilyState.value = activeTf.toFontFamily()
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
        val boldTf = if (newBold) base.bold() else base
        _initialFontFamily.value = boldTf.toFontFamily()
        val currentRawTf = rawWeightTypefaces.getOrNull(_selectedWeightIndex.value)?.second ?: regularTypeface
        if (currentRawTf != null) {
            val activeTf = if (newBold) currentRawTf.bold() else currentRawTf
            _fontFamilyState.value = activeTf.toFontFamily()
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
        if (isFontSavedInDownloads(fileName)) {
            fontDownloadManager.markAsDownloaded(fontItem.id)
        }
    }

    fun downloadFont() {
        val font = _uiState.value.fontDetail ?: return
        val state = _uiState.value
        viewModelScope.launch {
            // Never hand a null URL to the download manager
            if (font.fontFileUrl.isNullOrBlank()) {
                _eventFlow.emit(UiEvent.DownloadFailure(UserMessages.DOWNLOAD_LINK_UNAVAILABLE))
                return@launch
            }
            val isAlreadyDownloadedToday = userPreferences.isFontDownloadedToday(fontId)
            val limit = state.dailyFontLimit
            if (limit != null && state.downloadCount >= limit && !isAlreadyDownloadedToday) {
                _uiState.update { it.copy(showDownloadLimitBottomSheet = true) }
                return@launch
            }
            fontDownloadManager.downloadFont(font)
        }
    }

    fun dismissDownloadLimitBottomSheet() {
        _uiState.update { it.copy(showDownloadLimitBottomSheet = false) }
    }

    fun onWatchAdAndDownload(activity: PlatformActivity, adManager: AdManager) {
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