package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.NotificationTap
import com.urdufonts.app.domain.models.NotificationTapInbox
import com.urdufonts.app.domain.repo.PushRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Run at every app start, after signing in, after logging out and whenever the phone gets a new push token:
 * the backend then knows where to send this customer's notifications.
 */
class RegisterPushTokenUseCase(private val push: PushRepository) {
    suspend operator fun invoke(): Result<Unit> = push.register()
}

/** Run when the app comes back to the front: a registration that did not get through is sent again. */
class RetryPushRegistrationUseCase(private val push: PushRepository) {
    suspend operator fun invoke() {
        if (push.isRegistrationPending()) push.register()
    }
}

/** The on / off choice in Settings. */
class ObserveNotificationsEnabledUseCase(private val push: PushRepository) {
    operator fun invoke(): Flow<Boolean> = push.notificationsEnabled
}

class SetNotificationsEnabledUseCase(private val push: PushRepository) {
    suspend operator fun invoke(on: Boolean): Result<Unit> = push.setNotificationsEnabled(on)
}

/** The notification the customer tapped, kept until a screen has acted on it. */
class ObserveNotificationTapUseCase(private val inbox: NotificationTapInbox) {
    operator fun invoke(): StateFlow<NotificationTap?> = inbox.pending
}

/** The tap has been acted on: it is reported to the backend once and forgotten. */
class CompleteNotificationTapUseCase(
    private val inbox: NotificationTapInbox,
    private val push: PushRepository,
) {
    suspend operator fun invoke(tap: NotificationTap) {
        inbox.clear(tap)
        push.reportOpened(tap.id)
    }
}
