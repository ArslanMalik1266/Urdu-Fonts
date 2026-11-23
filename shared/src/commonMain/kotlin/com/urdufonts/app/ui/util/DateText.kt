package com.urdufonts.app.ui.util

import com.urdufonts.app.ui.support.kit.formatDay
import kotlin.time.Instant

/** An ISO-8601 time from the backend as a day in the phone's own style ("Dec 12, 2026"), or null if unreadable. */
fun formatIsoDay(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching { formatDay(Instant.parse(iso).toEpochMilliseconds()) }.getOrNull()
}

/** "12.3 MB" with one decimal, at least "0.1 MB" (no String.format in common code). */
fun formatMegabytes(bytes: Long): String {
    val tenths = (bytes * 10 / (1024L * 1024L))
    return if (tenths < 1) "0.1 MB" else "${tenths / 10}.${tenths % 10} MB"
}
