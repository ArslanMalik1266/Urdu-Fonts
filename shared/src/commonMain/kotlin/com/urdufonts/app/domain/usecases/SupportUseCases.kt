package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.AttachmentError
import com.urdufonts.app.domain.models.NewTicket
import com.urdufonts.app.domain.models.NewTicketError
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.domain.models.TicketLimits
import com.urdufonts.app.domain.repo.SupportRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveTicketsUseCase(private val repository: SupportRepository) {
    operator fun invoke(): StateFlow<List<Ticket>?> = repository.tickets
}

class RefreshTicketsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(): Result<List<Ticket>> = repository.refreshTickets()
}

/** A ticket's conversation: [cached] is what was loaded last time, [invoke] asks the backend. */
class GetTicketUseCase(private val repository: SupportRepository) {
    fun cached(id: Int): Ticket? = repository.cachedTicket(id)
    suspend operator fun invoke(id: Int): Result<Ticket> = repository.ticket(id)
}

/** Checks the new-ticket form. An empty answer means it can be sent. */
class ValidateNewTicketUseCase {
    operator fun invoke(ticket: NewTicket): Set<NewTicketError> = buildSet {
        if (ticket.category == null) add(NewTicketError.CATEGORY_MISSING)
        when {
            ticket.email.isBlank() -> add(NewTicketError.EMAIL_MISSING)
            !EMAIL.matches(ticket.email.trim()) -> add(NewTicketError.EMAIL_INVALID)
        }
        if (ticket.subject.isBlank()) add(NewTicketError.SUBJECT_MISSING)
        if (ticket.description.isBlank()) add(NewTicketError.DESCRIPTION_MISSING)
    }

    private companion object {
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")
    }
}

/** A picked file can be sent when it is one of the kinds the backend takes and not over its size limit. */
class ValidateAttachmentUseCase {
    operator fun invoke(draft: AttachmentDraft): AttachmentError? = when {
        draft.fileName.substringAfterLast('.', "").lowercase() !in TicketLimits.ATTACHMENT_EXTENSIONS ->
            AttachmentError.UNSUPPORTED_TYPE

        draft.sizeBytes > TicketLimits.ATTACHMENT_MAX_BYTES -> AttachmentError.TOO_LARGE
        else -> null
    }
}

/** Sends a new ticket, tidied up: trimmed, and cut to the lengths the backend accepts. */
class CreateTicketUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticket: NewTicket): Result<Ticket> = repository.create(
        ticket.copy(
            email = ticket.email.trim(),
            subject = ticket.subject.trim().take(TicketLimits.SUBJECT_MAX),
            description = ticket.description.trim().take(TicketLimits.DESCRIPTION_MAX),
        ),
    )
}

/**
 * Adds the customer's message to a ticket. Replying to a solved or closed ticket reopens it. The backend wants
 * some text with every reply, so a file sent on its own goes with its name as the text.
 */
class ReplyToTicketUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Int, body: String, attachment: AttachmentDraft?): Result<Ticket> =
        repository.reply(
            ticketId = ticketId,
            body = body.trim().ifBlank { attachment?.fileName.orEmpty() }.take(TicketLimits.REPLY_MAX),
            attachment = attachment,
        )
}

/**
 * The customer's answer to "Is your problem solved?". Yes closes the ticket; no sends it back to support,
 * with [reason] added to the conversation as the customer's message.
 */
class AnswerResolutionUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Int, solved: Boolean, reason: String = ""): Result<Ticket> =
        repository.answerResolution(ticketId, solved, reason.trim().take(TicketLimits.REASON_MAX))
}

/** Rates the help on a closed ticket, 1 to 5 stars with an optional comment. */
class RateTicketUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Int, stars: Int, comment: String = ""): Result<Ticket> =
        repository.rate(ticketId, stars.coerceIn(MIN_STARS, MAX_STARS), comment.trim().take(TicketLimits.COMMENT_MAX))

    private companion object {
        const val MIN_STARS = 1
        const val MAX_STARS = 5
    }
}

/** Where an attachment is on the phone, downloading it first if it is not there yet. */
class GetAttachmentFileUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Int, attachment: TicketAttachment): Result<String> =
        repository.attachmentFile(ticketId, attachment)
}
