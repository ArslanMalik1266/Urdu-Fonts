package com.urdufonts.app.ui.support.kit

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

private const val BYTES_PER_KB = 1024.0
private const val NANOS_PER_SECOND = 1_000_000_000f

/** A fully rounded shape, for pills and pill buttons. */
val PillShape = RoundedCornerShape(percent = 50)

/** 7.8 MB, 640 KB, 12 B. */
fun formatFileSize(bytes: Long): String {
    val kb = bytes / BYTES_PER_KB
    val mb = kb / BYTES_PER_KB
    return when {
        mb >= 1 -> "${oneDecimal(mb)} MB"
        kb >= 1 -> "${kb.toInt()} KB"
        else -> "$bytes B"
    }
}

private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).toInt()
    return "${tenths / 10}.${tenths % 10}"
}

/** A day ("Dec 12, 2026") in the phone's own style and language, from epoch milliseconds. */
expect fun formatDay(epochMillis: Long): String

/**
 * Seconds since the composable became visible, ticking every frame only while [running].
 * Read it inside draw or graphicsLayer blocks to skip recomposition.
 */
@Composable
internal fun rememberLoopClock(running: Boolean): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = -1L
        while (true) {
            withFrameNanos { now ->
                if (last >= 0) time.floatValue += (now - last) / NANOS_PER_SECOND
                last = now
            }
        }
    }
    return time
}
