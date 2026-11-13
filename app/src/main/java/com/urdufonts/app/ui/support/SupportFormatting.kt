package com.urdufonts.app.ui.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.urdufonts.app.ui.support.kit.formatDay
import com.urdufonts.app.ui.support.kit.formatFileSize
import com.urdufonts.app.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.res.stringResource
import kotlin.time.Instant

/** Which day a chat line falls on, relative to "now". */
sealed interface DaySection {
    data object Today : DaySection
    data object Yesterday : DaySection
    data class OnDate(val date: LocalDate) : DaySection
}

fun localDateTimeOf(millis: Long, zone: TimeZone): LocalDateTime =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)

fun daySectionOf(millis: Long, nowMillis: Long, zone: TimeZone): DaySection {
    val date = localDateTimeOf(millis, zone).date
    val today = localDateTimeOf(nowMillis, zone).date
    return when (date) {
        today -> DaySection.Today
        today.minus(DatePeriod(days = 1)) -> DaySection.Yesterday
        else -> DaySection.OnDate(date)
    }
}

/** "Dec 12, 2026" in the phone's own style. */
@Composable
fun dateText(date: LocalDate): String = formatDay(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())

/** "14:25" (24-hour, zero padded). */
fun clockText(millis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = localDateTimeOf(millis, zone)
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

@Composable
fun sectionTitle(section: DaySection): String = when (section) {
    DaySection.Today -> stringResource(R.string.section_today)
    DaySection.Yesterday -> stringResource(R.string.section_yesterday)
    is DaySection.OnDate -> dateText(section.date)
}

/** When a conversation last changed: the time today, "Yesterday", or the day. */
@Composable
internal fun madeText(millis: Long, nowMillis: Long): String {
    val zone = TimeZone.currentSystemDefault()
    return when (val section = daySectionOf(millis, nowMillis, zone)) {
        DaySection.Today -> clockText(millis, zone)
        else -> sectionTitle(section)
    }
}

/** "2.4 MB" */
fun fileSizeText(bytes: Long): String = formatFileSize(bytes)

/** A tap that also gives the light click the rest of the app's buttons give. */
@Composable
fun rememberHapticClick(onClick: () -> Unit): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics, onClick) {
        {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    }
}

/** One light tick (a pull reaching the refresh point). */
@Composable
fun rememberTickHaptic(): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) } }
}
