package com.urdufonts.app.ui.support

import org.jetbrains.compose.resources.StringResource
import com.urdufonts.app.resources.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.urdufonts.app.domain.models.TicketCategory
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.TicketStatus
import kotlin.time.Clock

internal val TicketCategory.label: StringResource
    get() = when (this) {
        TicketCategory.FONTS_DOWNLOADS -> Res.string.ticket_category_fonts
        TicketCategory.INSTALLING_FONTS -> Res.string.ticket_category_install
        TicketCategory.SEARCH_BROWSE -> Res.string.ticket_category_search
        TicketCategory.PREMIUM_PAYMENT -> Res.string.ticket_category_premium
        TicketCategory.ACCOUNT_LOGIN -> Res.string.ticket_category_account
        TicketCategory.ADS -> Res.string.ticket_category_ads
        TicketCategory.BUG_REPORT -> Res.string.ticket_category_bug
        TicketCategory.FEATURE_REQUEST -> Res.string.ticket_category_feature
        TicketCategory.OTHER -> Res.string.ticket_category_other
    }

/** The sentence shown for each problem with sign-in, support or push. */
internal val PremiumProblem.text: StringResource
    get() = when (this) {
        PremiumProblem.OFFLINE -> Res.string.problem_offline
        PremiumProblem.SESSION_EXPIRED -> Res.string.problem_session_expired
        PremiumProblem.SIGN_IN_UNAVAILABLE -> Res.string.problem_sign_in_unavailable
        PremiumProblem.SIGN_IN_REJECTED -> Res.string.problem_sign_in_rejected
        PremiumProblem.STORE_BUSY -> Res.string.problem_store_busy
        else -> Res.string.problem_unknown
    }

internal val AttachmentProblem.text: StringResource
    get() = when (this) {
        AttachmentProblem.TOO_LARGE -> Res.string.ticket_error_attachment_large
        AttachmentProblem.UNSUPPORTED_TYPE -> Res.string.ticket_error_attachment_type
        AttachmentProblem.UNREADABLE -> Res.string.ticket_error_attachment_read
    }

internal val TicketStatus.label: StringResource
    get() = when (this) {
        TicketStatus.OPEN -> Res.string.support_status_open
        TicketStatus.WAITING_FOR_YOU -> Res.string.support_status_waiting_you
        TicketStatus.WAITING_FOR_SUPPORT -> Res.string.support_status_waiting_support
        TicketStatus.RESOLVED -> Res.string.support_status_resolved
        TicketStatus.CLOSED -> Res.string.support_status_closed
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
