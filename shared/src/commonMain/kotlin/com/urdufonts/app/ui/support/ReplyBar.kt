package com.urdufonts.app.ui.support

import com.urdufonts.app.resources.*
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.ui.support.kit.AppIcon
import com.urdufonts.app.ui.support.kit.addPressEffect
import com.urdufonts.app.ui.support.kit.Dimens
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// The message box grows with what is typed up to this many lines, then scrolls inside itself
private const val FIELD_MAX_LINES = 8
private const val MENU_FROM_SCALE = 0.85f
private const val MENU_CLOSE_MILLIS = 160
private val MENU_REOPEN_AFTER = 350.milliseconds

/**
 * Where the customer writes: the attach button (it opens Gallery / Files floating just above it), the message
 * box, and the send button, which turns blue once there is something to send. A picked file waits above the
 * box until it is sent or taken off. Sending never blocks the bar: the next message can be written at once.
 */
@Composable
internal fun ReplyBar(
    text: String,
    attachment: AttachmentDraft?,
    canSend: Boolean,
    onTextChange: (String) -> Unit,
    onGallery: () -> Unit,
    onFiles: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var choosingSource by remember { mutableStateOf(false) }
    var menuClosedAt by remember { mutableStateOf<TimeMark?>(null) }
    val canAttach = attachment == null
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8), modifier = modifier.fillMaxWidth()) {
        if (attachment != null) {
            FileCard(
                icon = if (attachment.isImage) Res.drawable.ic_sp_image else Res.drawable.ic_sp_file,
                title = attachment.fileName,
                subtitle = fileSizeText(attachment.sizeBytes),
            ) {
                RoundAction(onClick = onRemoveAttachment, enabled = true, container = colors.surface) {
                    AppIcon(Res.drawable.ic_sp_close, stringResource(Res.string.ticket_attach_remove), tint = colors.onSurfaceVariant, size = Dimens.SupportIcon)
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            Box {
                RoundAction(
                    // A tap on the button while the menu is open has already closed it, as any touch outside the
                    // menu does; that same tap must not open it again
                    onClick = {
                        val justClosed = menuClosedAt?.let { it.elapsedNow() < MENU_REOPEN_AFTER } == true
                        if (!justClosed) choosingSource = true
                    },
                    enabled = canAttach,
                    container = colors.surface,
                    outlined = true,
                ) {
                    AppIcon(
                        Res.drawable.ic_sp_attachment,
                        stringResource(Res.string.chat_attach),
                        tint = if (canAttach) colors.onSurface else colors.onSurfaceVariant,
                        size = Dimens.SupportIcon,
                    )
                }
                SourcePopup(
                    open = choosingSource && canAttach,
                    onDismiss = {
                        choosingSource = false
                        menuClosedAt = TimeSource.Monotonic.markNow()
                    },
                    onGallery = onGallery,
                    onFiles = onFiles,
                )
            }
            MessageBox(text = text, onTextChange = onTextChange, modifier = Modifier.weight(1f))
            val sendColor by animateColorAsState(if (canSend) colors.primary else colors.surfaceVariant, label = "send")
            RoundAction(onClick = onSend, enabled = canSend, container = sendColor) {
                AppIcon(Res.drawable.ic_sp_send, stringResource(Res.string.chat_send), tint = if (canSend) colors.onPrimary else colors.onSurfaceVariant, size = Dimens.SupportIcon)
            }
        }
    }
}

/**
 * The Gallery / Files choice as a drop-up: it floats in its own layer just above the attach button, so nothing
 * in the conversation moves, and a tap anywhere else closes it. A choice is acted on once it has closed, so the
 * picker never opens over a menu still on screen.
 */
@Composable
private fun SourcePopup(open: Boolean, onDismiss: () -> Unit, onGallery: () -> Unit, onFiles: () -> Unit) {
    val offset = with(LocalDensity.current) {
        IntOffset(x = -0.dp.roundToPx(), y = -(Dimens.ButtonHeight + Dimens.Space8 - 0.dp).roundToPx())
    }
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = open
    var chosen by remember { mutableStateOf<(() -> Unit)?>(null) }
    val closed = shown.isIdle && !shown.currentState
    LaunchedEffect(closed) {
        if (closed) {
            chosen?.invoke()
            chosen = null
        }
    }
    // Its layer stays until the closing animation has finished
    if (closed && !open) return
    val corner = TransformOrigin(0f, 1f)
    Popup(alignment = Alignment.BottomStart, offset = offset, onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visibleState = shown,
            enter = fadeIn() + scaleIn(initialScale = MENU_FROM_SCALE, transformOrigin = corner),
            exit = fadeOut(tween(MENU_CLOSE_MILLIS)) + scaleOut(tween(MENU_CLOSE_MILLIS), targetScale = MENU_FROM_SCALE, transformOrigin = corner),
        ) {
            AttachmentSourceMenu(
                onGallery = {
                    chosen = onGallery
                    onDismiss()
                },
                onFiles = {
                    chosen = onFiles
                    onDismiss()
                },
                modifier = Modifier.padding(0.dp),
            )
        }
    }
}

@Composable
private fun MessageBox(text: String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.ReplyFieldRadius)
    val textStyle = MaterialTheme.typography.bodyMedium
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        maxLines = FIELD_MAX_LINES,
        textStyle = textStyle.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier,
        decorationBox = { field ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.ButtonHeight)
                    .clip(shape)
                    .background(colors.surface)
                    .border(Dimens.SupportBorder, colors.outlineVariant, shape)
                    .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
            ) {
                if (text.isEmpty()) Text(text = stringResource(Res.string.chat_reply_hint), style = textStyle, color = colors.onSurfaceVariant)
                field()
            }
        },
    )
}

/** A round button the height of the message box, for attaching, removing and sending. */
@Composable
private fun RoundAction(onClick: () -> Unit, enabled: Boolean, container: Color, outlined: Boolean = false, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Dimens.ButtonHeight)
            .addPressEffect(onClick = rememberHapticClick(onClick), enabled = enabled)
            .clip(CircleShape)
            .background(container)
            .then(if (outlined) Modifier.border(Dimens.SupportBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape) else Modifier),
    ) { content() }
}
