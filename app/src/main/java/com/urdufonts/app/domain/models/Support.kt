package com.urdufonts.app.domain.models

/** What a ticket can be about. [slug] is the category's name in the backend's dashboard (Support → Categories). */
enum class TicketCategory(val slug: String) {
    FONTS_DOWNLOADS("fonts-downloads"),
    INSTALLING_FONTS("installing-fonts"),
    SEARCH_BROWSE("search-browse"),
    PREMIUM_PAYMENT("premium-payment"),
    ACCOUNT_LOGIN("account-login"),
    ADS("ads"),
    BUG_REPORT("bug-report"),
    FEATURE_REQUEST("feature-request"),
    OTHER("other"),
}

/** The backend's limits on what a customer can send. */
object TicketLimits {
    const val SUBJECT_MAX = 255
    const val DESCRIPTION_MAX = 10_000
    const val REPLY_MAX = 5_000
    const val REASON_MAX = 2_000
    const val COMMENT_MAX = 1_000
    const val ATTACHMENT_MAX_BYTES = 10L * 1024 * 1024
    val ATTACHMENT_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "pdf", "txt", "log", "json")
}

/** A file the customer picked to send, held in memory until it is sent. */
class AttachmentDraft(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    /** The file's real size. It differs from [bytes] only for a file too large to be read at all. */
    val sizeBytes: Long = bytes.size.toLong(),
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

enum class AttachmentError { TOO_LARGE, UNSUPPORTED_TYPE, UNREADABLE }

/** The new-ticket form as the customer filled it in. */
data class NewTicket(
    val category: TicketCategory?,
    val email: String,
    val subject: String,
    val description: String,
    val attachment: AttachmentDraft?,
)

/** What stops a new ticket from being sent. Each one is shown under its own field. */
enum class NewTicketError {
    CATEGORY_MISSING,
    EMAIL_MISSING,
    EMAIL_INVALID,
    SUBJECT_MISSING,
    DESCRIPTION_MISSING,
}

/** Where a support ticket stands, from the customer's side. */
enum class TicketStatus {
    /** New: nobody from support has answered yet. */
    OPEN,

    /** Support has answered and is waiting for the customer. */
    WAITING_FOR_YOU,

    /** The customer has answered and support has the ticket. */
    WAITING_FOR_SUPPORT,

    /** Support thinks it is solved and is waiting for the customer to confirm. */
    RESOLVED,
    CLOSED,
}

/** Who wrote a line of the conversation. */
enum class MessageAuthor { ME, SUPPORT, SYSTEM }

data class TicketAttachment(
    val id: Int,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

data class TicketMessage(
    val id: Int,
    val author: MessageAuthor,
    val body: String,
    /** Epoch milliseconds; 0 when the backend sent no readable time. */
    val sentAt: Long,
    val attachments: List<TicketAttachment>,
) {
    /**
     * What the customer actually wrote. A file sent on its own carries its name as its text (the backend
     * insists on some), and that is not worth showing under the file itself.
     */
    val text: String get() = body.takeUnless { written -> attachments.any { it.fileName == written } }.orEmpty()
}

/**
 * One conversation with support. [messages] is filled when the ticket itself was opened; a ticket from the
 * list carries only [lastMessage].
 */
data class Ticket(
    val id: Int,
    val subject: String,
    val status: TicketStatus,
    val categoryName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessage: String,
    val lastMessageAuthor: MessageAuthor,
    val messages: List<TicketMessage>,
    /** Support marked it solved: the customer is asked "Is your problem solved?". */
    val awaitingConfirmation: Boolean,
    /** When an unanswered "solved?" closes the ticket by itself; 0 when it will not. */
    val autoClosesAt: Long,
    /** 1 to 5 once rated, otherwise null. */
    val rating: Int?,
    val canRate: Boolean,
) {
    /** Something is waiting for the customer: an answer to read, or "is it solved?". */
    val needsCustomer: Boolean get() = status == TicketStatus.WAITING_FOR_YOU || awaitingConfirmation
}
