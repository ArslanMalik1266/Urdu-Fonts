package com.urdufonts.app.ui.util

import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException

/**
 * The only error texts users see. Raw exception messages ("URL is null", "Download failed: 404", ...)
 * are logged, never shown.
 */
object UserMessages {
    const val DOWNLOAD_LINK_UNAVAILABLE = "Download link temporarily unavailable. Please try again later."
    const val FONT_FILE_FAILED = "Unable to load font file. Please check your connection or contact support."
    const val NO_CONNECTION = "No internet connection. Please check your connection and try again."
    const val GENERIC = "Something went wrong. Please try again."

    /** Network problems get the connection message; anything else gets [fallback]. */
    fun forError(e: Throwable?, fallback: String = GENERIC): String = when (e) {
        is IOException, is TimeoutCancellationException -> NO_CONNECTION
        else -> fallback
    }
}
