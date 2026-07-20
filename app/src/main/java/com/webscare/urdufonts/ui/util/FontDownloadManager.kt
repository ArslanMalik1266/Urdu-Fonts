package com.webscare.urdufonts.ui.util

import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.usecases.DownloadFontToDeviceUseCase
import com.webscare.urdufonts.ui.detailScreen.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FontDownloadManager(
    private val downloadFontToDeviceUseCase: DownloadFontToDeviceUseCase
) {
    // 🟢 Global Scope that survives ViewModel clearance
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Maps to track multiple download progress & states by fontId
    private val _downloadProgresses = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val downloadProgresses: StateFlow<Map<Int, Float>> = _downloadProgresses.asStateFlow()

    private val _downloadStates = MutableStateFlow<Map<Int, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Int, DownloadState>> = _downloadStates.asStateFlow()

    // Events to dispatch one-time notifications (Success/Failure)
    sealed class DownloadEvent {
        data class Success(val fontId: Int, val message: String) : DownloadEvent()
        data class Failure(val fontId: Int, val error: String) : DownloadEvent()
    }

    private val _events = MutableSharedFlow<DownloadEvent>()
    val events = _events.asSharedFlow()

    fun downloadFont(font: FontItem) {
        if (_downloadStates.value[font.id] == DownloadState.DOWNLOADING) return

        // Set initial downloading states
        _downloadStates.update { it + (font.id to DownloadState.DOWNLOADING) }
        _downloadProgresses.update { it + (font.id to 0f) }

        // Launch in the persistent Application scope
        applicationScope.launch {
            downloadFontToDeviceUseCase(font, onProgress = { progress ->
                _downloadProgresses.update { it + (font.id to progress) }
            }).onSuccess {
                _downloadStates.update { it + (font.id to DownloadState.DOWNLOADED) }
                _downloadProgresses.update { it + (font.id to 1.0f) }

                val subfolder = "Downloads/UrduFonts/${font.name.replace(" ", "_")}"
                _events.emit(DownloadEvent.Success(font.id, "Download Complete!|$subfolder"))
            }.onFailure { error ->
                _downloadStates.update { it + (font.id to DownloadState.IDLE) }
                _downloadProgresses.update { it + (font.id to 0f) }

                _events.emit(DownloadEvent.Failure(font.id, "Download failed: ${error.localizedMessage}"))
            }
        }
    }

    // Call this if cache check confirms the font is already on disk
    fun markAsDownloaded(fontId: Int) {
        _downloadStates.update { it + (fontId to DownloadState.DOWNLOADED) }
        _downloadProgresses.update { it + (fontId to 1.0f) }
    }
}
