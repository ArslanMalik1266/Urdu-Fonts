package com.urdufonts.app.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.toPremiumProblem
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.AttachmentError
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.domain.models.TicketLimits
import com.urdufonts.app.domain.usecases.AnswerResolutionUseCase
import com.urdufonts.app.domain.usecases.GetAttachmentFileUseCase
import com.urdufonts.app.domain.usecases.GetTicketUseCase
import com.urdufonts.app.domain.usecases.RateTicketUseCase
import com.urdufonts.app.domain.usecases.ReplyToTicketUseCase
import com.urdufonts.app.domain.usecases.ValidateAttachmentUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A message the customer has sent that the backend has not confirmed yet. It is shown in the
 * conversation at once; [failed] means sending did not work and it can be tried again.
 */
data class OutgoingMessage(
    val localId: Long,
    val text: String,
    val attachment: AttachmentDraft?,
    val failed: Boolean = false
)

/**
 * One conversation. [ticket] is null until it has loaded: with [loadProblem] set that is the
 * error state, without it the conversation is still loading.
 */
data class TicketChatUiState(
    val ticket: Ticket? = null,
    val loadProblem: PremiumProblem? = null,
    val reply: String = "",
    val attachment: AttachmentDraft? = null,
    /** Sent by the customer, on their way: drawn after the confirmed messages, in the order sent. */
    val outgoing: List<OutgoingMessage> = emptyList(),
    /**
     * Confirmed messages that were sent from this screen, by message id, with what they were while
     * on their way: the list keeps drawing them as the same bubble, so nothing jumps when they land.
     */
    val delivered: Map<Int, OutgoingMessage> = emptyMap(),
    /** The customer pulled up at the end of the conversation and it is being checked for new messages. */
    val refreshing: Boolean = false,
    /** The answer to "is it solved?" or a rating is on its way. */
    val answering: Boolean = false,
    /** Where each picture attachment is on the phone, by attachment id, once downloaded. */
    val pictures: Map<Int, String> = emptyMap(),
    /** The attachment being fetched because the customer tapped it. */
    val openingAttachmentId: Int? = null
) {
    val isLoading: Boolean get() = ticket == null && loadProblem == null
    val canSend: Boolean get() = reply.isNotBlank() || attachment != null
}

sealed interface TicketChatEvent {
    data class Failed(val problem: PremiumProblem) : TicketChatEvent
    data class AttachmentRefused(val problem: AttachmentProblem) : TicketChatEvent
    data class OpenFile(val path: String, val mimeType: String) : TicketChatEvent
}

class TicketChatViewModel(
    private val ticketId: Int,
    private val getTicket: GetTicketUseCase,
    private val replyToTicket: ReplyToTicketUseCase,
    private val answerResolution: AnswerResolutionUseCase,
    private val rateTicket: RateTicketUseCase,
    private val getAttachmentFile: GetAttachmentFileUseCase,
    private val validateAttachment: ValidateAttachmentUseCase
) : ViewModel() {

    // Opened before in this session: shown at once while the fresh copy is fetched
    private val _uiState = MutableStateFlow(TicketChatUiState(ticket = getTicket.cached(ticketId)))
    val uiState: StateFlow<TicketChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<TicketChatEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Messages go to the backend one at a time, so they arrive in the order they were written
    private val sendOrder = Mutex()
    private var nextLocalId = 0L

    init {
        _uiState.value.ticket?.let { fetchPictures(it) }
        load()
    }

    fun load() {
        _uiState.update { it.copy(loadProblem = null) }
        viewModelScope.launch {
            getTicket(ticketId).fold(
                onSuccess = { show(it) },
                // A conversation already on screen stays; the problem is only shown instead of nothing
                onFailure = { error -> _uiState.update { it.copy(loadProblem = error.toPremiumProblem()) } }
            )
        }
    }

    /**
     * The customer pulled up past the newest message: look for new ones now. The sign that it is
     * looking stays up for a moment even when the answer is instant, so it never just flickers.
     */
    fun refresh() {
        if (_uiState.value.refreshing || _uiState.value.ticket == null) return
        _uiState.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            val shownFor = launch { delay(REFRESH_SHOWN_MILLIS) }
            getTicket(ticketId).onSuccess { show(it) }
            shownFor.join()
            _uiState.update { it.copy(refreshing = false) }
        }
    }

    fun onReplyChange(value: String) = _uiState.update { it.copy(reply = value.take(TicketLimits.REPLY_MAX)) }

    fun onAttachmentPicked(draft: AttachmentDraft) {
        when (validateAttachment(draft)) {
            AttachmentError.TOO_LARGE -> refuse(AttachmentProblem.TOO_LARGE)
            AttachmentError.UNSUPPORTED_TYPE -> refuse(AttachmentProblem.UNSUPPORTED_TYPE)
            AttachmentError.UNREADABLE -> refuse(AttachmentProblem.UNREADABLE)
            null -> _uiState.update { it.copy(attachment = draft) }
        }
    }

    fun onAttachmentUnreadable() = refuse(AttachmentProblem.UNREADABLE)

    fun onAttachmentRemoved() = _uiState.update { it.copy(attachment = null) }

    /**
     * The message goes into the conversation straight away and the box is cleared for the next
     * one; it is marked as sent (it gets its time) once the backend has it.
     */
    fun send() {
        val state = _uiState.value
        if (!state.canSend) return
        val message = OutgoingMessage(localId = nextLocalId++, text = state.reply.trim(), attachment = state.attachment)
        _uiState.update { it.copy(reply = "", attachment = null, outgoing = it.outgoing + message) }
        deliver(message)
    }

    /** A message that could not be sent is tried again where it stands. */
    fun retry(localId: Long) {
        val message = _uiState.value.outgoing.firstOrNull { it.localId == localId && it.failed } ?: return
        val again = message.copy(failed = false)
        _uiState.update { state -> state.copy(outgoing = state.outgoing.map { if (it.localId == localId) again else it }) }
        deliver(again)
    }

    private fun deliver(message: OutgoingMessage) {
        viewModelScope.launch {
            sendOrder.withLock {
                replyToTicket(ticketId, message.text, message.attachment).fold(
                    onSuccess = { ticket ->
                        // The confirmed message takes the place of the waiting one in the same update.
                        // It is the newest message of the conversation that is not already accounted for.
                        _uiState.update { state ->
                            val landed = ticket.messages.lastOrNull { it.id !in state.delivered }
                            state.copy(
                                ticket = ticket,
                                loadProblem = null,
                                outgoing = state.outgoing.filterNot { it.localId == message.localId },
                                delivered = if (landed == null) state.delivered else state.delivered + (landed.id to message)
                            )
                        }
                        fetchPictures(ticket)
                    },
                    // Said under the message itself ("Not sent"), not in a message of its own
                    onFailure = {
                        _uiState.update { state ->
                            state.copy(
                                outgoing = state.outgoing.map { if (it.localId == message.localId) it.copy(failed = true) else it }
                            )
                        }
                    }
                )
            }
        }
    }

    /** The answer to "Is your problem solved?". [reason] is what is still wrong, for a "no". */
    fun answerSolved(solved: Boolean, reason: String = "") = answer { answerResolution(ticketId, solved, reason) }

    fun rate(stars: Int, comment: String) = answer { rateTicket(ticketId, stars, comment) }

    fun openAttachment(attachment: TicketAttachment) {
        if (_uiState.value.openingAttachmentId != null) return
        _uiState.update { it.copy(openingAttachmentId = attachment.id) }
        viewModelScope.launch {
            getAttachmentFile(ticketId, attachment).fold(
                onSuccess = { _events.send(TicketChatEvent.OpenFile(it, attachment.mimeType)) },
                onFailure = { _events.send(TicketChatEvent.Failed(it.toPremiumProblem())) }
            )
            _uiState.update { it.copy(openingAttachmentId = null) }
        }
    }

    private fun answer(action: suspend () -> Result<Ticket>) {
        if (_uiState.value.answering) return
        _uiState.update { it.copy(answering = true) }
        viewModelScope.launch {
            action().fold(
                onSuccess = { show(it) },
                onFailure = { error ->
                    _events.send(TicketChatEvent.Failed(error.toPremiumProblem()))
                    // Staff or the auto-close changed the ticket meanwhile: show what it is now
                    if (error.toPremiumProblem() == PremiumProblem.TICKET_CHANGED) load()
                }
            )
            _uiState.update { it.copy(answering = false) }
        }
    }

    private fun show(ticket: Ticket) {
        _uiState.update { it.copy(ticket = ticket, loadProblem = null) }
        fetchPictures(ticket)
    }

    /** Picture attachments are shown in the conversation itself, so they are fetched as it loads. */
    private fun fetchPictures(ticket: Ticket) {
        ticket.messages.flatMap { it.attachments }
            .filter { it.isImage && it.id !in _uiState.value.pictures }
            .forEach { picture ->
                viewModelScope.launch {
                    getAttachmentFile(ticketId, picture).onSuccess { path ->
                        _uiState.update { it.copy(pictures = it.pictures + (picture.id to path)) }
                    }
                }
            }
    }

    private fun refuse(problem: AttachmentProblem) {
        viewModelScope.launch { _events.send(TicketChatEvent.AttachmentRefused(problem)) }
    }

    private companion object {
        const val REFRESH_SHOWN_MILLIS = 700L
    }
}
