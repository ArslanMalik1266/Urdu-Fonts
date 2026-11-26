package com.urdufonts.app.ui.support

import org.jetbrains.compose.resources.getString
import com.urdufonts.app.resources.*
import com.urdufonts.app.ui.support.kit.EmptyArt
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.urdufonts.app.ui.support.kit.LocalAppSnackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.TicketStatus
import com.urdufonts.app.ui.support.kit.AppTopBar
import com.urdufonts.app.ui.support.kit.EmptyState
import com.urdufonts.app.ui.support.kit.SkeletonBox
import com.urdufonts.app.ui.support.kit.ShimmerHost
import com.urdufonts.app.ui.support.kit.TextAction
import com.urdufonts.app.ui.support.kit.Dimens
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * One conversation with support: the messages, and at the bottom whatever the customer can do next (reply,
 * answer "Is your problem solved?", or rate the help). It loads fresh every time it opens; a conversation
 * opened earlier in the session shows at once meanwhile.
 */
@Composable
fun TicketChatScreen(ticketId: Int, onBack: () -> Unit) {
    val viewModel = koinViewModel<TicketChatViewModel>(key = "ticket-$ticketId") { parametersOf(ticketId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The app's one snackbar, above the screen
    val snackbarHostState = LocalAppSnackbar.current
    val listState = rememberLazyListState()
    var ratingStars by remember { mutableStateOf<Int?>(null) }
    var askingReason by remember { mutableStateOf(false) }
    val pickers = rememberAttachmentPickers(onPicked = viewModel::onAttachmentPicked, onFailed = viewModel::onAttachmentUnreadable)
    val openFile = rememberCachedFileOpener()
    val focusManager = LocalFocusManager.current
    val ticket = state.ticket

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TicketChatEvent.Failed -> snackbarHostState.showSnackbar(org.jetbrains.compose.resources.getString(event.problem.text))
                is TicketChatEvent.AttachmentRefused -> snackbarHostState.showSnackbar(org.jetbrains.compose.resources.getString(event.problem.text))
                is TicketChatEvent.OpenFile ->
                    if (!openFile(event.path, event.mimeType)) snackbarHostState.showSnackbar(getString(Res.string.chat_open_failed))
            }
        }
    }
    // The list opens at its newest end by itself. A new message is put there too: it grows in from nothing, so
    // the conversation is pushed up by it instead of being scrolled
    val messageCount = (ticket?.messages?.size ?: 0) + state.outgoing.size
    LaunchedEffect(messageCount) {
        if (messageCount > 0) listState.requestScrollToItem(0)
    }

    Scaffold(
        // The whole screen rides the keyboard as one piece (the bar and the conversation together), instead of
        // the bottom bar growing on its own while the list is measured again: that made the screen flicker
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = ticket?.subject ?: stringResource(Res.string.chat_support_name),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (ticket != null) {
                Box {
                    when {
                        ticket.awaitingConfirmation -> ResolutionCard(
                            autoClosesAt = ticket.autoClosesAt,
                            working = state.answering,
                            onSolved = { viewModel.answerSolved(true) },
                            onNotSolved = { askingReason = true },
                        )
                        // Solved and closed: the conversation is over, so no reply bar. The stars while they can
                        // still be given, otherwise only the note
                        ticket.status == TicketStatus.CLOSED || ticket.status == TicketStatus.RESOLVED -> {
                            if (ticket.canRate && ticket.rating == null) {
                                RatingCard(onPick = { ratingStars = it })
                            } else {
                                DoneNote()
                            }
                        }
                        else -> Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime)).padding(Dimens.Space16)) {
                            ReplyBar(
                                text = state.reply,
                                attachment = state.attachment,
                                canSend = state.canSend,
                                onTextChange = viewModel::onReplyChange,
                                onGallery = pickers.gallery,
                                onFiles = pickers.files,
                                onRemoveAttachment = viewModel::onAttachmentRemoved,
                                onSend = viewModel::send,
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // A tap on the conversation puts the keyboard away
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            when {
                ticket != null -> ChatConversation(
                    ticket = ticket,
                    outgoing = state.outgoing,
                    delivered = state.delivered,
                    refreshing = state.refreshing,
                    pictures = state.pictures,
                    openingAttachmentId = state.openingAttachmentId,
                    listState = listState,
                    onOpenAttachment = viewModel::openAttachment,
                    onRetry = viewModel::retry,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                )
                state.isLoading -> ChatSkeleton()
                else -> EmptyState(
                    icon = Res.drawable.ic_sp_alert,
                    title = stringResource(Res.string.chat_load_error),
                    message = stringResource(state.loadProblem?.text ?: Res.string.chat_load_error),
                    modifier = Modifier.fillMaxSize(),
                    actionText = stringResource(Res.string.action_try_again),
                    onAction = viewModel::load,
                    art = EmptyArt.Alert,
                )
            }
        }
    }

    ratingStars?.let { stars ->
        RatingSheet(
            initialStars = stars,
            onDismiss = { ratingStars = null },
            onSend = { picked, comment ->
                ratingStars = null
                viewModel.rate(picked, comment)
            },
        )
    }
    if (askingReason) {
        NotSolvedSheet(
            onDismiss = { askingReason = false },
            onSend = { reason ->
                askingReason = false
                viewModel.answerSolved(false, reason)
            },
        )
    }
}

/** The line at the bottom of a conversation that is solved and closed. */
@Composable
private fun DoneNote() {
    Text(
        text = stringResource(Res.string.chat_resolved_done_note),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime)).padding(Dimens.Space16),
    )
}

/** The conversation's shape while it loads: bubbles from both sides in skeleton grey. */
@Composable
private fun ChatSkeleton() {
    ShimmerHost {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
            SKELETON_BUBBLES.forEach { (mine, width) ->
                Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                    SkeletonBox(Modifier.fillMaxWidth(width).height(Dimens.ChatSkeletonBubble), shape = MaterialTheme.shapes.medium)
                }
            }
        }
    }
}

/** Which side each placeholder bubble is on, and how much of the row it takes. */
private val SKELETON_BUBBLES = listOf(false to 0.6f, true to 0.5f, false to 0.7f, true to 0.4f)
