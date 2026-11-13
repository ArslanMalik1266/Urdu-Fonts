package com.urdufonts.app.ui.support

import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.ui.support.kit.AppIcon
import com.urdufonts.app.ui.support.kit.Dimens
import kotlinx.datetime.TimeZone
import androidx.compose.ui.res.stringResource
import com.urdufonts.app.R

// A bubble never spans the whole row, so whose side it is on stays obvious
private const val BUBBLE_MAX_WIDTH = 0.82f
private const val JUST_NOW_MILLIS = 60_000L

// The customer's bubble is a little darker towards the bottom, so it has some depth
private const val BUBBLE_SHADE = 0.14f
private const val ON_BUBBLE_FADED = 0.75f

/** The centred pill that starts each day of the conversation: "Today", "Yesterday", or the date. */
@Composable
internal fun DayDivider(millis: Long) {
    val now = remember(millis) { nowMillis() }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.Space8)) {
        Text(
            text = sectionTitle(daySectionOf(millis, now, TimeZone.currentSystemDefault())),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = Dimens.Space12, vertical = Dimens.Space4),
        )
    }
}

/** A status line the backend wrote ("Support marked this ticket as solved"): small and centred, not a bubble. */
@Composable
internal fun SystemLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.Space32, vertical = Dimens.Space4),
    )
}

/** When a message was sent: "Just now" for its first minute, then the time on the clock. */
@Composable
private fun sentLabel(millis: Long): String {
    if (millis <= 0L) return ""
    val fresh = remember(millis) { nowMillis() - millis < JUST_NOW_MILLIS }
    return if (fresh) stringResource(R.string.time_ago_just_now) else clockText(millis)
}

/**
 * One message, from the moment the customer sends it to long after. The customer's own sit at the end in the
 * brand blue; support's sit at the start in white, the first of a run under the team's name. While it is on
 * its way the bubble carries a small clock; the backend having it turns that into its time, or it says "Not
 * sent" and a tap tries again. Messages sent together share one time, shown on the last of them; a tap on any
 * other shows that one's time, and a second tap hides it.
 */
@Composable
internal fun ChatBubble(
    line: ChatLine,
    pictures: Map<Int, String>,
    openingAttachmentId: Int?,
    onOpenAttachment: (TicketAttachment) -> Unit,
    onRetry: (localId: Long) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val message = line.message
    val waiting = line.outgoing.takeIf { message == null }
    val mine = line.mine
    val text = message?.text ?: waiting?.text.orEmpty()
    val failed = waiting?.failed == true
    val sending = waiting != null && !failed
    var timeAsked by remember { mutableStateOf(false) }
    val time = if (message != null) sentLabel(message.sentAt) else ""
    val timeShown = time.isNotEmpty() && (line.showTime || timeAsked)
    val side = if (mine) Alignment.End else Alignment.Start
    Column(
        horizontalAlignment = side,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (line.startsGroup) Dimens.Space8 else 0.dp)
            .then(if (waiting != null && failed) Modifier.plainTap { onRetry(waiting.localId) } else Modifier),
    ) {
        if (!mine && line.startsGroup) {
            Text(
                text = stringResource(R.string.chat_support_name),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                modifier = Modifier.padding(start = Dimens.Space4, bottom = Dimens.Space4),
            )
        }
        Column(horizontalAlignment = side, modifier = Modifier.fillMaxWidth(BUBBLE_MAX_WIDTH)) {
            val hasFile = BubbleFiles(line, pictures, openingAttachmentId, onOpenAttachment)
            if (text.isNotBlank()) {
                if (hasFile) Spacer(Modifier.height(Dimens.Space4))
                BubbleText(
                    text = text,
                    mine = mine,
                    tail = line.tail,
                    onTap = if (message != null && !line.showTime) ({ timeAsked = !timeAsked }) else null,
                ) {
                    BubbleFooter(visible = sending || timeShown) {
                        val faded = if (mine) colors.onPrimary.copy(alpha = ON_BUBBLE_FADED) else colors.onSurfaceVariant
                        if (sending) {
                            AppIcon(R.drawable.ic_sp_clock, stringResource(R.string.chat_sending), tint = faded, size = Dimens.ChatClock)
                        } else {
                            SmallNote(text = time, color = faded)
                        }
                    }
                }
            } else {
                // A file sent on its own has no bubble to carry the time, so it sits under the file
                BubbleFooter(visible = timeShown) {
                    SmallNote(text = time, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = Dimens.Space4))
                }
            }
            BubbleFooter(visible = failed) {
                Text(
                    text = stringResource(R.string.chat_not_sent),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.error,
                    modifier = Modifier.padding(start = Dimens.Space4, end = Dimens.Space4, top = Dimens.Space4),
                )
            }
        }
    }
}

/** The files of a message, one under the other. Says whether there were any. */
@Composable
private fun BubbleFiles(
    line: ChatLine,
    pictures: Map<Int, String>,
    openingAttachmentId: Int?,
    onOpenAttachment: (TicketAttachment) -> Unit,
): Boolean {
    val message = line.message
    val draft = line.outgoing?.attachment
    if (message == null) {
        draft?.let { OutgoingAttachment(it) }
        return draft != null
    }
    message.attachments.forEachIndexed { index, attachment ->
        if (index > 0) Spacer(Modifier.height(Dimens.Space4))
        MessageAttachment(
            attachment = attachment,
            picturePath = pictures[attachment.id],
            // Sent from this screen: the picture is already in memory, so it never blanks to a shimmer
            sentBytes = draft?.bytes?.takeIf { draft.isImage && attachment.isImage },
            opening = openingAttachmentId == attachment.id,
            onOpen = { onOpenAttachment(attachment) },
        )
    }
    return message.attachments.isNotEmpty()
}

/** Something small under or inside a bubble that comes and goes: it opens and closes its own space. */
@Composable
private fun BubbleFooter(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandIn(expandFrom = Alignment.TopEnd),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopEnd),
    ) { content() }
}

@Composable
private fun SmallNote(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1, modifier = modifier)
}

/** The bubble itself: the words, with [footer] (the time, or the sending clock) small in its bottom corner. */
@Composable
private fun BubbleText(text: String, mine: Boolean, tail: Boolean, onTap: (() -> Unit)?, footer: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(
        topStart = Dimens.BubbleRadius,
        topEnd = Dimens.BubbleRadius,
        bottomStart = if (!mine && tail) Dimens.BubbleTailRadius else Dimens.BubbleRadius,
        bottomEnd = if (mine && tail) Dimens.BubbleTailRadius else Dimens.BubbleRadius,
    )
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .then(if (onTap != null) Modifier.plainTap { onTap() } else Modifier)
            .clip(shape)
            .then(
                if (mine) {
                    Modifier.background(Brush.verticalGradient(listOf(colors.primary, lerp(colors.primary, Color.Black, BUBBLE_SHADE))))
                } else {
                    Modifier.background(colors.surface).border(Dimens.SupportBorder, colors.outlineVariant, shape)
                },
            )
            .padding(horizontal = Dimens.BubblePaddingH, vertical = Dimens.Space8),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (mine) colors.onPrimary else colors.onSurface,
            modifier = Modifier.align(Alignment.Start),
        )
        footer()
    }
}

/** A tap with no press effect: for bubbles and stars, which react with their own change instead. */
internal fun Modifier.plainTap(onTap: () -> Unit): Modifier = pointerInput(onTap) { detectTapGestures { onTap() } }
