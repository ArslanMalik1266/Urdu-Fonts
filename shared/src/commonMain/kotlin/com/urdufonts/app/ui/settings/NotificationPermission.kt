package com.urdufonts.app.ui.settings

import androidx.compose.runtime.Composable

/**
 * Whether the phone lets the app show notifications, and the two ways of asking for it. [ask] shows the
 * system's own question where there is one and does nothing otherwise. [turnOn] is for a switch the customer
 * pressed: it asks, or, when the phone no longer shows the question, opens the app's notification settings.
 * [granted] is read again whenever the app comes back to the front.
 */
class NotificationPermission(
    val granted: Boolean,
    val ask: () -> Unit,
    val turnOn: () -> Unit,
)

/** Android: POST_NOTIFICATIONS (13+) and the app's notification settings. iOS: UNUserNotificationCenter. */
@Composable
expect fun rememberNotificationPermission(): NotificationPermission
