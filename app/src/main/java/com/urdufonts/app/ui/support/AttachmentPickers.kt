package com.urdufonts.app.ui.support

import androidx.compose.runtime.Composable
import com.urdufonts.app.domain.models.AttachmentDraft

/** What went wrong with the file the customer picked. */
enum class AttachmentProblem { TOO_LARGE, UNSUPPORTED_TYPE, UNREADABLE }

/** The two ways to pick a file to send: [gallery] opens the phone's photo picker, [files] its file picker. */
class AttachmentPickers(val gallery: () -> Unit, val files: () -> Unit)

/**
 * [onPicked] receives the file the customer chose; [onFailed] is called when it could not be read. Closing a
 * picker without choosing calls neither.
 */

