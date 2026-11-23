package com.urdufonts.app.data.iap

import kotlinx.coroutines.IO

import com.urdufonts.app.platform.AppDirectories
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.DeviceDetails
import com.urdufonts.app.domain.models.MessageAuthor
import com.urdufonts.app.domain.models.NewTicket
import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.domain.models.TicketMessage
import com.urdufonts.app.domain.models.TicketStatus
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import com.urdufonts.app.domain.repo.SupportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.write

class SupportRepositoryImpl(
    private val directories: AppDirectories,
    private val api: IapApi,
    private val caller: IapCaller,
    private val identity: IapIdentityProvider,
    private val deviceDetails: DeviceDetailsSource,
) : SupportRepository {

    private val _tickets = MutableStateFlow<List<Ticket>?>(null)
    override val tickets: StateFlow<List<Ticket>?> = _tickets.asStateFlow()

    // Conversations opened in this session, so going back into one shows it at once
    private val opened = mutableMapOf<Int, Ticket>()

    override fun cachedTicket(id: Int): Ticket? = opened[id]

    override suspend fun refreshTickets(): Result<List<Ticket>> = run { who ->
        val all = mutableListOf<Ticket>()
        var page = 1
        do {
            val answer = api.tickets(who.bearer, who.externalUserId, who.deviceSecret, page, PAGE_SIZE)
            all += answer.tickets.orEmpty().map { it.toDomain() }
            val more = page < (answer.pagination?.totalPages ?: 1)
            page++
        } while (more && page <= MAX_PAGES)
        all.sortedByDescending { it.updatedAt }.also { _tickets.value = it }
    }

    override suspend fun ticket(id: Int): Result<Ticket> = run { who -> load(who, id) }

    override suspend fun create(ticket: NewTicket): Result<Ticket> = run { who ->
        val fields = buildMap {
            who.externalUserId?.let { put("external_user_id", it) }
            put("subject", ticket.subject)
            put("body", ticket.description)
            put("email", ticket.email)
            ticket.category?.let { put("category_slug", it.slug) }
            put("platform", IapConfig.PLATFORM)
            // Which app version and phone the ticket comes from, so support does not have to ask
            deviceDetails.current().toMetadata().forEach { (name, value) -> put("metadata[$name]", value) }
        }
        val created = api.createTicket(who.bearer, who.externalUserId, who.deviceSecret, fields, ticket.attachment)
            .ticket?.toDomain() ?: throw PremiumException(PremiumProblem.UNKNOWN)
        remember(created)
        created
    }

    override suspend fun reply(ticketId: Int, body: String, attachment: AttachmentDraft?): Result<Ticket> = run { who ->
        val fields = buildMap {
            who.externalUserId?.let { put("external_user_id", it) }
            put("body", body)
        }
        val answer = api.reply(who.bearer, who.externalUserId, who.deviceSecret, ticketId, fields, attachment)
        val sent = answer.message?.toDomain()
        val before = opened[ticketId]
        if (sent == null || before == null) {
            load(who, ticketId)
        } else {
            // The backend answers with the new message alone: it is added to the conversation in hand
            before.copy(
                status = answer.ticketStatus.toTicketStatus(),
                messages = before.messages + sent,
                lastMessage = sent.body,
                lastMessageAuthor = sent.author,
                updatedAt = maxOf(before.updatedAt, sent.sentAt),
                awaitingConfirmation = false
            ).also { remember(it) }
        }
    }

    override suspend fun answerResolution(ticketId: Int, solved: Boolean, reason: String): Result<Ticket> =
        run { who ->
            api.resolution(
                who.bearer, who.externalUserId, who.deviceSecret, ticketId,
                TicketResolutionRequestDto(solved = solved, reason = reason.takeIf { !solved && it.isNotBlank() })
            )
            // The whole conversation again: the backend has added its own status line to it
            load(who, ticketId)
        }

    override suspend fun rate(ticketId: Int, stars: Int, comment: String): Result<Ticket> = run { who ->
        api.rating(
            who.bearer, who.externalUserId, who.deviceSecret, ticketId,
            TicketRatingRequestDto(stars = stars, comment = comment.takeIf { it.isNotBlank() })
        )
        load(who, ticketId)
    }

    override suspend fun attachmentFile(ticketId: Int, attachment: TicketAttachment): Result<String> = run { who ->
        val name = "ticket${ticketId}_${attachment.id}_" + attachment.fileName.filter { it.isLetterOrDigit() || it == '.' }
        val dir = Path(directories.cacheDir, "support").also { SystemFileSystem.createDirectories(it) }
        val file = Path(dir, name)
        val cachedSize = SystemFileSystem.metadataOrNull(file)?.size ?: 0L
        if (cachedSize > 0) return@run file.toString()
        val bytes = api.attachment(who.bearer, who.externalUserId, who.deviceSecret, ticketId, attachment.id)
        withContext(Dispatchers.IO) { SystemFileSystem.sink(file).buffered().use { it.write(bytes) } }
        file.toString()
    }

    private suspend fun load(who: IapIdentity, id: Int): Ticket {
        val ticket = api.ticket(who.bearer, who.externalUserId, who.deviceSecret, id).ticket?.toDomain()
            ?: throw PremiumException(PremiumProblem.UNKNOWN)
        remember(ticket)
        return ticket
    }

    /** Keeps the opened conversation and brings the list's entry for it up to date. */
    private fun remember(ticket: Ticket) {
        opened[ticket.id] = ticket
        _tickets.update { list ->
            if (list == null) null else (list.filterNot { it.id == ticket.id } + ticket).sortedByDescending { it.updatedAt }
        }
    }

    /** Runs [block] as the current identity. A guest whose secret the backend refuses is set up again once. */
    private suspend fun <T> run(block: suspend (IapIdentity) -> T): Result<T> = caller.result {
        try {
            block(identity.current())
        } catch (e: PremiumException) {
            if (e.problem != PremiumProblem.INVALID_DEVICE_SECRET) throw e
            identity.forgetGuest()
            block(identity.current())
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 4
    }
}

/**
 * The phone's details as the ticket's `metadata`, under the names the support team sees in
 * IAP Manager. A detail the phone could not give is left out rather than sent empty.
 */
fun DeviceDetails.toMetadata(): Map<String, String> = mapOf(
    "app_version" to appVersion,
    "device_brand" to brand,
    "device_model" to model,
    "os_version" to osVersion,
    "language" to language,
    "timezone" to timeZone
).filterValues { it.isNotBlank() }

// ── Mapping ───────────────────────────────────────────────────────────────

/** An ISO-8601 time from the backend as epoch milliseconds; 0 when missing or unreadable. */
fun String?.isoToEpochMillis(): Long {
    if (isNullOrBlank()) return 0L
    return runCatching { Instant.parse(this).toEpochMilliseconds() }.getOrDefault(0L)
}

fun TicketDto.toDomain(): Ticket {
    val thread = messages.orEmpty().map { it.toDomain() }
    val last = latestMessage?.toDomain() ?: thread.lastOrNull()
    val created = createdAt.isoToEpochMillis()
    return Ticket(
        id = id ?: 0,
        subject = subject.orEmpty(),
        status = status.toTicketStatus(),
        categoryName = category?.name.orEmpty(),
        createdAt = created,
        // The newest message is the truest "last activity"; the backend's own stamp can lag behind it
        updatedAt = maxOf(updatedAt.isoToEpochMillis(), last?.sentAt ?: 0L, created),
        lastMessage = last?.body.orEmpty(),
        lastMessageAuthor = last?.author ?: MessageAuthor.ME,
        messages = thread,
        awaitingConfirmation = resolution?.awaitingConfirmation == true,
        autoClosesAt = resolution?.autoCloseAt.isoToEpochMillis(),
        rating = rating?.stars,
        canRate = canRate == true
    )
}

fun TicketMessageDto.toDomain(): TicketMessage = TicketMessage(
    id = id ?: 0,
    author = authorType.toAuthor(),
    body = body.orEmpty(),
    sentAt = createdAt.isoToEpochMillis(),
    attachments = attachments.orEmpty().map {
        TicketAttachment(
            id = it.id ?: 0,
            fileName = it.filename.orEmpty(),
            mimeType = it.mimeType.orEmpty(),
            sizeBytes = it.sizeBytes ?: 0L
        )
    }
)

fun String?.toTicketStatus(): TicketStatus = when (this) {
    "pending_customer" -> TicketStatus.WAITING_FOR_YOU
    "pending_internal" -> TicketStatus.WAITING_FOR_SUPPORT
    "resolved" -> TicketStatus.RESOLVED
    "closed" -> TicketStatus.CLOSED
    else -> TicketStatus.OPEN
}

/** The customer's own lines are "member" (or a guest); "system" is a status line; anyone else is support. */
private fun String?.toAuthor(): MessageAuthor = when (this) {
    "member", "guest", "user", "customer" -> MessageAuthor.ME
    "system" -> MessageAuthor.SYSTEM
    else -> MessageAuthor.SUPPORT
}
