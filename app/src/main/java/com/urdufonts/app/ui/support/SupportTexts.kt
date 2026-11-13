package com.urdufonts.app.ui.support

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.urdufonts.app.domain.models.TicketCategory
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.TicketStatus
import com.urdufonts.app.R
import kotlin.time.Clock

internal val TicketCategory.label: Int
    get() = when (this) {
        TicketCategory.FONTS_DOWNLOADS -> R.string.ticket_category_fonts
        TicketCategory.INSTALLING_FONTS -> R.string.ticket_category_install
        TicketCategory.SEARCH_BROWSE -> R.string.ticket_category_search
        TicketCategory.PREMIUM_PAYMENT -> R.string.ticket_category_premium
        TicketCategory.ACCOUNT_LOGIN -> R.string.ticket_category_account
        TicketCategory.ADS -> R.string.ticket_category_ads
        TicketCategory.BUG_REPORT -> R.string.ticket_category_bug
        TicketCategory.FEATURE_REQUEST -> R.string.ticket_category_feature
        TicketCategory.OTHER -> R.string.ticket_category_other
    }

/** The sentence shown for each problem with sign-in, support or push. */
internal val PremiumProblem.text: Int
    get() = when (this) {
        PremiumProblem.OFFLINE -> R.string.problem_offline
        PremiumProblem.SESSION_EXPIRED -> R.string.problem_session_expired
        PremiumProblem.SIGN_IN_UNAVAILABLE -> R.string.problem_sign_in_unavailable
        PremiumProblem.SIGN_IN_REJECTED -> R.string.problem_sign_in_rejected
        PremiumProblem.STORE_BUSY -> R.string.problem_store_busy
        else -> R.string.problem_unknown
    }

internal val AttachmentProblem.text: Int
    get() = when (this) {
        AttachmentProblem.TOO_LARGE -> R.string.ticket_error_attachment_large
        AttachmentProblem.UNSUPPORTED_TYPE -> R.string.ticket_error_attachment_type
        AttachmentProblem.UNREADABLE -> R.string.ticket_error_attachment_read
    }

internal val TicketStatus.label: Int
    get() = when (this) {
        TicketStatus.OPEN -> R.string.support_status_open
        TicketStatus.WAITING_FOR_YOU -> R.string.support_status_waiting_you
        TicketStatus.WAITING_FOR_SUPPORT -> R.string.support_status_waiting_support
        TicketStatus.RESOLVED -> R.string.support_status_resolved
        TicketStatus.CLOSED -> R.string.support_status_closed
    }

/** A status pill's background and text. */
internal data class StatusTint(val container: Color, val content: Color)

/** The tint a status is shown in: the accent asks for the customer, a neutral one is with support or over. */
@Composable
internal fun TicketStatus.tint(): StatusTint {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        TicketStatus.WAITING_FOR_YOU, TicketStatus.RESOLVED -> StatusTint(colors.primaryContainer, colors.primary)
        TicketStatus.OPEN, TicketStatus.WAITING_FOR_SUPPORT -> StatusTint(colors.secondaryContainer, colors.onSecondaryContainer)
        TicketStatus.CLOSED -> StatusTint(colors.surfaceVariant, colors.onSurfaceVariant)
    }
}

internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
