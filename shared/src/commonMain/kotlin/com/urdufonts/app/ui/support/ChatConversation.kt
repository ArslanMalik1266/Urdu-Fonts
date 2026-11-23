package com.urdufonts.app.ui.support

import com.urdufonts.app.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import com.urdufonts.app.domain.models.MessageAuthor
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.domain.models.TicketStatus
import com.urdufonts.app.ui.support.kit.LoadingSpinner
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.ui.support.kit.springOverscroll
import org.jetbrains.compose.resources.stringResource

private const val POP_FROM_SCALE = 0.8f

// The pull has to fall back below this share of the distance before the tick can be felt again
private const val RE_ARM_SHARE = 0.6f
private const val FULL_SWEEP = 360f
private const val START_ANGLE = -90f
private const val RING_READY_SCALE = 1.15f
private const val RING_MIN_SCALE = 0.6f

/**
 * The conversation: every message under the day it was sent, with consecutive messages from one side drawn as
 * a group, then the messages still on their way. The list is anchored to its newest end, so it rides up and
 * down with the keyboard and a new message grows in at the bottom, pushing the older ones up. Pulling up past
 * the newest message and letting go calls [onRefresh].
 */
@Composable
internal fun ChatConversation(
    ticket: Ticket,
    outgoing: List<OutgoingMessage>,
    delivered: Map<Int, OutgoingMessage>,
    refreshing: Boolean,
    pictures: Map<Int, String>,
    openingAttachmentId: Int?,
    listState: LazyListState,
    onOpenAttachment: (TicketAttachment) -> Unit,
    onRetry: (localId: Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages = ticket.messages
    val lines = remember(messages, outgoing, delivered) { chatLines(messages, outgoing, delivered) }
    // What was already there when the conversation opened is simply there; only later rows pop in
    val seen = remember { lines.mapTo(HashSet()) { it.key } }
    val pull = remember { Animatable(0f) }
    val refreshAt = with(LocalDensity.current) { Dimens.ChatRefreshPull.toPx() }
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    // A tick the moment the pull is far enough that letting go will look for new messages; given once per pull
    val tick = rememberTickHaptic()
    LaunchedEffect(refreshAt) {
        var armed = true
        snapshotFlow { -pull.value }.collect { pulled ->
            if (pulled >= refreshAt && armed) {
                armed = false
                tick()
            } else if (pulled < refreshAt * RE_ARM_SHARE) {
                armed = true
            }
        }
    }
    val letGo = remember(refreshAt) {
        object : NestedScrollConnection {
            // The finger lifts while the list is still held up far enough
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull.value <= -refreshAt) currentOnRefresh()
                return Velocity.Zero
            }
        }
    }
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            PullRing(
                progress = { if (refreshing) 0f else (-pull.value / refreshAt).coerceIn(0f, 1f) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            LazyColumn(
                state = listState,
                reverseLayout = true,
                // A conversation shorter than the screen still starts at the top
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(start = Dimens.Space16, end = Dimens.Space16, top = Dimens.Space4, bottom = Dimens.Space16),
                modifier = Modifier.fillMaxSize().nestedScroll(letGo).springOverscroll(pull = pull),
            ) {
                ticket.rating?.let { stars -> item(key = "rated") { RatedLine(stars) } }
                // Nobody from support has written yet: say the message arrived and where the answer will come
                if (ticket.status == TicketStatus.OPEN && messages.none { it.author == MessageAuthor.SUPPORT }) {
                    item(key = "waiting") { SystemLine(stringResource(Res.string.chat_waiting_note)) }
                }
                items(lines, key = { it.key }) { line ->
                    ChatRow(
                        line = line,
                        isNew = remember { seen.add(line.key) },
                        pictures = pictures,
                        openingAttachmentId = openingAttachmentId,
                        onOpenAttachment = onOpenAttachment,
                        onRetry = onRetry,
                    )
                }
            }
        }
        AnimatedVisibility(visible = refreshing) {
            // Looking for new messages because the customer asked: an action in progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(top = Dimens.Space4, bottom = Dimens.Space12)) {
                LoadingSpinner(size = Dimens.SupportLoader)
            }
        }
    }
}

/** One row of the list. A new one ([isNew]) opens its own space and grows out of its sender's corner. */
@Composable
private fun ChatRow(
    line: ChatLine,
    isNew: Boolean,
    pictures: Map<Int, String>,
    openingAttachmentId: Int?,
    onOpenAttachment: (TicketAttachment) -> Unit,
    onRetry: (localId: Long) -> Unit,
) {
    val shown = remember { MutableTransitionState(!isNew).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = shown,
        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top) +
            scaleIn(
                animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
                initialScale = POP_FROM_SCALE,
                transformOrigin = TransformOrigin(if (line.mine) 1f else 0f, 1f),
            ) + fadeIn(),
        exit = ExitTransition.None,
    ) {
        Column(modifier = Modifier.padding(top = Dimens.Space4)) {
            val message = line.message
            if (message != null && line.newDay) DayDivider(message.sentAt)
            if (message?.author == MessageAuthor.SYSTEM) {
                SystemLine(message.body)
            } else {
                ChatBubble(line, pictures, openingAttachmentId, onOpenAttachment, onRetry)
            }
        }
    }
}

/**
 * What shows in the gap under the newest message while the conversation is pulled up: a ring that closes as
 * the pull nears the point where letting go looks for new messages. [progress] is read while drawing only, so
 * following the finger recomposes nothing.
 */
@Composable
private fun PullRing(progress: () -> Float, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .padding(bottom = Dimens.Space16)
            .size(Dimens.SupportLoader)
            .graphicsLayer {
                val pulled = progress()
                alpha = pulled
                // Grows into place, with a small bump once it is far enough to let go
                val scale = if (pulled >= 1f) RING_READY_SCALE else RING_MIN_SCALE + (1f - RING_MIN_SCALE) * pulled
                scaleX = scale
                scaleY = scale
            },
    ) {
        val stroke = (Dimens.Hairline * 2).toPx()
        drawArc(
            color = color,
            startAngle = START_ANGLE,
            sweepAngle = FULL_SWEEP * progress(),
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
