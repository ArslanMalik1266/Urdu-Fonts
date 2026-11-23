package com.urdufonts.app.ui.support

import com.urdufonts.app.resources.*
import androidx.compose.foundation.layout.height
import com.urdufonts.app.ui.support.kit.EmptyArt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.MessageAuthor
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketStatus
import com.urdufonts.app.ui.support.kit.AppIcon
import com.urdufonts.app.ui.support.kit.AppTopBar
import com.urdufonts.app.ui.support.kit.EmptyState
import com.urdufonts.app.ui.support.kit.PrimaryButton
import com.urdufonts.app.ui.support.kit.SkeletonBox
import com.urdufonts.app.ui.support.kit.ShimmerHost
import com.urdufonts.app.ui.support.kit.SkeletonBox
import com.urdufonts.app.ui.support.kit.TextAction
import com.urdufonts.app.ui.support.kit.addPressEffect
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.ui.support.kit.springOverscroll
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Help & Support: the customer's conversations with the support team, newest first, and the round [+] to
 * start a new one. Loaded once when it opens (and on "Try again"); a staff reply arrives as a notification
 * (the phone's question about showing them is asked on Home).
 */
@Composable
fun TicketListScreen(
    onBack: () -> Unit,
    onNewTicket: () -> Unit,
    onOpenTicket: (Int) -> Unit,
    viewModel: TicketListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tickets = state.tickets
    Scaffold(
        topBar = { AppTopBar(title = stringResource(Res.string.support_title), onBack = onBack) },
        // The empty state has its own button, so the floating one would only repeat it
        floatingActionButton = { if (!tickets.isNullOrEmpty()) NewTicketButton(onNewTicket) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> TicketListSkeleton()
                tickets == null -> EmptyState(
                    icon = Res.drawable.ic_sp_alert,
                    title = stringResource(Res.string.support_error_title),
                    message = stringResource(state.problem?.text ?: Res.string.support_error_title),
                    modifier = Modifier.fillMaxSize(),
                    actionText = stringResource(Res.string.action_try_again),
                    onAction = viewModel::refresh,
                    art = EmptyArt.Alert,
                )
                tickets.isEmpty() -> EmptyState(
                    icon = Res.drawable.ic_sp_message,
                    title = stringResource(Res.string.support_empty_title),
                    message = stringResource(Res.string.support_empty_message),
                    modifier = Modifier.fillMaxSize(),
                    actionText = stringResource(Res.string.support_empty_action),
                    onAction = onNewTicket,
                    art = EmptyArt.Float,
                )
                else -> {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
                        // Room at the end for the floating button, so it never covers the last conversation
                        contentPadding = PaddingValues(start = Dimens.Space16, end = Dimens.Space16, top = Dimens.Space8, bottom = Dimens.SupportFabClearance),
                        modifier = Modifier.fillMaxSize().clipToBounds().springOverscroll(),
                    ) {
                        items(tickets, key = { it.id }) { ticket ->
                            TicketRow(ticket, onClick = { onOpenTicket(ticket.id) }, modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}

/** One conversation: what it is about, its last line, where it stands and when it last moved. */
@Composable
private fun TicketRow(ticket: Ticket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val now = remember(ticket.updatedAt) { nowMillis() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        modifier = modifier
            .fillMaxWidth()
            .addPressEffect(onClick = rememberHapticClick(onClick))
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .padding(Dimens.Space12),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2), modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                // Something is waiting for the customer: a dot, like an unread chat
                if (ticket.needsCustomer) Box(Modifier.size(Dimens.UnreadDot).clip(CircleShape).background(colors.primary))
                Text(
                    text = ticket.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (ticket.needsCustomer) FontWeight.Bold else FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (ticket.lastMessage.isNotBlank()) {
                val line = ticket.lastMessage.replace('\n', ' ')
                Text(
                    text = when (ticket.lastMessageAuthor) {
                        MessageAuthor.ME -> stringResource(Res.string.support_preview_you, line)
                        MessageAuthor.SUPPORT -> stringResource(Res.string.support_preview_support, line)
                        // A status line ("Ticket closed") was written by nobody, so it has no name in front
                        MessageAuthor.SYSTEM -> line
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
                modifier = Modifier.padding(top = Dimens.Space4),
            ) {
                TicketStatusChip(ticket.status)
                Text(text = madeText(ticket.updatedAt, now), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
        AppIcon(Res.drawable.ic_sp_chevron_right, null, tint = colors.onSurfaceVariant, size = Dimens.IconSmall)
    }
}

/** A ticket's status as a small tinted pill. */
@Composable
internal fun TicketStatusChip(status: TicketStatus, modifier: Modifier = Modifier) {
    val tint = status.tint()
    Text(
        text = stringResource(status.label),
        style = MaterialTheme.typography.labelSmall,
        color = tint.content,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(tint.container).padding(horizontal = Dimens.Space8, vertical = Dimens.Space2),
    )
}

/** The round [+]: a primary circle with a white ring and a soft shadow, like the bottom bar's. */
@Composable
private fun NewTicketButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Dimens.SupportFab)
            .addPressEffect(onClick = rememberHapticClick(onClick))
            .shadow(Dimens.Space8, CircleShape, ambientColor = colors.primary, spotColor = colors.primary)
            .clip(CircleShape)
            .background(colors.primary)
            .border(Dimens.Hairline * 2, colors.surface, CircleShape),
    ) {
        AppIcon(Res.drawable.ic_sp_add, stringResource(Res.string.support_new_ticket), tint = colors.onPrimary)
    }
}

/** The list's shape while it loads: white cards with a subject, a line and a status pill. */
@Composable
private fun TicketListSkeleton() {
    ShimmerHost {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            repeat(SKELETON_ROWS) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface).padding(Dimens.Space12),
                ) {
                    SkeletonBox(Modifier.fillMaxWidth(0.55f).height(Dimens.Space12))
                    SkeletonBox(Modifier.fillMaxWidth(0.8f).height(Dimens.SkeletonLineSmall))
                    SkeletonBox(Modifier.size(width = Dimens.SkeletonChipWidth, height = Dimens.Space16), shape = CircleShape)
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 5
