package com.urdufonts.app.ui.util

import kotlinx.coroutines.IO
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.usecases.DownloadFontToDeviceUseCase
import com.urdufonts.app.ui.detailScreen.DownloadState
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
import kotlinx.coroutines.delay
import com.urdufonts.app.platform.PlatformFile
import com.urdufonts.app.platform.currentTimeMillis

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
            val startTime = currentTimeMillis()
            var actualProgress = 0f
            var actualDownloadFinished = false
            var downloadResult: Result<PlatformFile>? = null

            // 1. Launch the actual download in a background coroutine
            val downloadJob = launch(Dispatchers.IO) {
                downloadResult = downloadFontToDeviceUseCase(font, onProgress = { progress ->
                    actualProgress = progress
                })
                actualDownloadFinished = true
            }

            // 2. Visual progress coordination loop (runs at ~30 FPS)
            while (true) {
                val elapsedTime = currentTimeMillis() - startTime
                val maxAllowedProgress = elapsedTime.toFloat() / 3000f // 3000ms minimum duration (3 seconds)
                val reportedProgress = minOf(actualProgress, maxAllowedProgress)

                _downloadProgresses.update { it + (font.id to reportedProgress) }

                if (actualDownloadFinished) {
                    val result = downloadResult
                    if (result != null) {
                        if (result.isFailure) {
                            // If failed, exit immediately to report the error without delaying the user
                            downloadJob.cancel()
                            _downloadStates.update { it + (font.id to DownloadState.IDLE) }
                            _downloadProgresses.update { it + (font.id to 0f) }
                            val error = result.exceptionOrNull()
                            val rawError = error?.message ?: "Unknown error"
                            com.urdufonts.app.platform.AppLog.e("FontDownload", "Download failed for ${font.id}: $rawError", error)
                            val cleanError = when {
                                rawError.contains("Failed to build unique file", ignoreCase = true) ||
                                rawError.contains("bucket_display_name", ignoreCase = true) ->
                                    "Unable to save font to Downloads. Please check storage permissions."
                                font.fontFileUrl.isNullOrBlank() -> UserMessages.DOWNLOAD_LINK_UNAVAILABLE
                                // Never show the raw exception text to users
                                else -> UserMessages.forError(error, fallback = UserMessages.FONT_FILE_FAILED)
                            }
                            _events.emit(DownloadEvent.Failure(font.id, cleanError))
                            break
                        } else if (reportedProgress >= 1.0f) {
                            // If succeeded and visual progress reached 100%, finalize state
                            _downloadStates.update { it + (font.id to DownloadState.DOWNLOADED) }
                            _downloadProgresses.update { it + (font.id to 1.0f) }
                            val subfolder = "Downloads/UrduFonts/${font.name.replace(" ", "_")}"
                            _events.emit(DownloadEvent.Success(font.id, "Download Complete!|$subfolder"))
                            break
                        }
                    }
                }

                delay(30) // ~30 FPS frame rate
            }
        }
    }

    // Call this if cache check confirms the font is already on disk
    fun markAsDownloaded(fontId: Int) {
        _downloadStates.update { it + (fontId to DownloadState.DOWNLOADED) }
        _downloadProgresses.update { it + (fontId to 1.0f) }
    }
}
