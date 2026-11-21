package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.NewTicket
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketAttachment
import kotlinx.coroutines.flow.StateFlow

/** The customer's conversations with support. Works for a signed-in member and for a guest alike. */
interface SupportRepository {
    /** The customer's tickets, newest first; null until they have been loaded once. */
    val tickets: StateFlow<List<Ticket>?>

    suspend fun refreshTickets(): Result<List<Ticket>>

    /** One ticket with its whole conversation, fresh from the backend. */
    suspend fun ticket(id: Int): Result<Ticket>

    /** The conversation as it was last loaded in this session, if it was. */
    fun cachedTicket(id: Int): Ticket?

    suspend fun create(ticket: NewTicket): Result<Ticket>
    suspend fun reply(ticketId: Int, body: String, attachment: AttachmentDraft?): Result<Ticket>

    /** The customer's answer to "Is your problem solved?"; [reason] says what is still wrong. */
    suspend fun answerResolution(ticketId: Int, solved: Boolean, reason: String): Result<Ticket>
    suspend fun rate(ticketId: Int, stars: Int, comment: String): Result<Ticket>

    /** Downloads an attachment once and returns where it is kept on the phone. */
    suspend fun attachmentFile(ticketId: Int, attachment: TicketAttachment): Result<String>
}
