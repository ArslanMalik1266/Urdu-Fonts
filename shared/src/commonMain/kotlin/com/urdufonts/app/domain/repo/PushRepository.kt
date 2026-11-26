package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.DeviceDetails
import kotlinx.coroutines.flow.Flow

interface PushRepository {
    /** The customer's own on / off choice for notifications (Settings). */
    val notificationsEnabled: Flow<Boolean>

    /**
     * Turning on registers this phone again; turning off tells the backend to stop sending to it. A failure
     * to turn off leaves the choice as it was.
     */
    suspend fun setNotificationsEnabled(on: Boolean): Result<Unit>

    /** Tells the backend which phone to send this customer's notifications to. */
    /** [force]: send even when this token is already registered (a new sign-in moves it to that account). */
    suspend fun register(force: Boolean = false): Result<Unit>

    /** The last registration has not reached the backend yet. */
    suspend fun isRegistrationPending(): Boolean

    /** Stops notifications for whoever is signed in (or the guest) on this phone. */
    suspend fun unregister(): Result<Unit>

    /** Tells the backend the customer tapped the notification [id]. Never fails the caller. */
    suspend fun reportOpened(id: String)
}

/** Where the phone's push token comes from. Null while push is not set up on this phone or build. */
interface PushTokenSource {
    suspend fun token(): String?
}

/** The phone's details, read fresh each time. Platform-specific. */
interface DeviceDetailsSource {
    fun current(): DeviceDetails
}
