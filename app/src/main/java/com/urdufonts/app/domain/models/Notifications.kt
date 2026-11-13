package com.urdufonts.app.domain.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A notification the customer tapped: [id] is reported back to the backend, the rest says where the tap leads. */
data class NotificationTap(
    val id: String,
    val type: String = "",
    val category: String = "",
    val deepLink: String? = null,
    val ticketId: Int? = null,
)

/**
 * Holds the notification the customer tapped until the app is ready to act on it: the tap can arrive while
 * the splash is still up, before any screen exists to open.
 */
class NotificationTapInbox {
    private val _pending = MutableStateFlow<NotificationTap?>(null)
    val pending: StateFlow<NotificationTap?> = _pending.asStateFlow()

    fun post(tap: NotificationTap) {
        _pending.value = tap
    }

    fun clear(tap: NotificationTap) {
        _pending.compareAndSet(tap, null)
    }
}
