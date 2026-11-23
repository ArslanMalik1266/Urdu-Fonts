package com.urdufonts.app.ui.support

import com.urdufonts.app.domain.models.MessageAuthor
import com.urdufonts.app.domain.models.TicketMessage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

private const val MINUTE_MILLIS = 60_000L

/**
 * One row of the conversation. [message] is set once the backend has it; [outgoing] is what the customer sent
 * from this screen, and stays set after it is confirmed so the row keeps its [key] and is drawn as the same
 * bubble all the way through.
 */
internal data class ChatLine(
    val key: String,
    val message: TicketMessage?,
    val outgoing: OutgoingMessage?,
    val newDay: Boolean,
    val startsGroup: Boolean,
    val tail: Boolean,
    val showTime: Boolean,
) {
    val mine: Boolean get() = message == null || message.author == MessageAuthor.ME
}

private fun dayOf(millis: Long, zone: TimeZone): LocalDate = localDateTimeOf(millis, zone).date

/** Sent by the same side within the same minute: they read as one burst and share one time. */
private fun TicketMessage.sentTogetherWith(other: TicketMessage?): Boolean =
    other != null && other.author == author && other.sentAt / MINUTE_MILLIS == sentAt / MINUTE_MILLIS

/**
 * The conversation as rows, newest first (the list is anchored to its newest end): every confirmed message
 * with how it sits in its day and its run, then the messages still on their way.
 */
internal fun chatLines(
    messages: List<TicketMessage>,
    outgoing: List<OutgoingMessage>,
    delivered: Map<Int, OutgoingMessage>,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): List<ChatLine> {
    val confirmed = messages.mapIndexed { index, message ->
        val before = messages.getOrNull(index - 1)
        val after = messages.getOrNull(index + 1)
        val day = dayOf(message.sentAt, zone)
        val newDay = before == null || dayOf(before.sentAt, zone) != day
        val nextIsNewDay = after != null && dayOf(after.sentAt, zone) != day
        // The customer's own run carries on into the messages still being sent
        val runContinues = after == null && message.author == MessageAuthor.ME && outgoing.isNotEmpty()
        val sentHere = delivered[message.id]
        ChatLine(
            key = if (sentHere != null) "o${sentHere.localId}" else "m${message.id}",
            message = message,
            outgoing = sentHere,
            newDay = newDay && message.sentAt > 0L,
            startsGroup = newDay || before.author != message.author,
            tail = !runContinues && (nextIsNewDay || after?.author != message.author),
            showTime = nextIsNewDay || !message.sentTogetherWith(after),
        )
    }
    val lastIsMine = messages.lastOrNull()?.author == MessageAuthor.ME
    val waiting = outgoing.mapIndexed { index, message ->
        ChatLine(
            key = "o${message.localId}",
            message = null,
            outgoing = message,
            newDay = false,
            startsGroup = index == 0 && !lastIsMine,
            tail = index == outgoing.lastIndex,
            showTime = false,
        )
    }
    return (confirmed + waiting).asReversed()
}
