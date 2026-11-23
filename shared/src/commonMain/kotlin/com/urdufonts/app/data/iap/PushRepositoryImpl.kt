package com.urdufonts.app.data.iap

import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import com.urdufonts.app.domain.repo.PushRepository
import com.urdufonts.app.domain.repo.PushTokenSource
import com.urdufonts.app.platform.AppLog as Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PushRepositoryImpl(
    private val api: IapApi,
    private val caller: IapCaller,
    private val identity: IapIdentityProvider,
    private val tokens: PushTokenSource,
    private val preferences: IapPreferences,
    private val deviceDetails: DeviceDetailsSource,
) : PushRepository {

    // One registration at a time, each with whoever the customer is at that moment
    private val registering = Mutex()

    override val notificationsEnabled: Flow<Boolean> = preferences.pushEnabled

    override suspend fun setNotificationsEnabled(on: Boolean): Result<Unit> {
        val before = preferences.isPushEnabled()
        preferences.setPushEnabled(on)
        if (on) {
            // A registration that does not get through now is sent again when the app next comes to the front
            register()
            return Result.success(Unit)
        }
        return unregister().onFailure { preferences.setPushEnabled(before) }
    }

    override suspend fun register(): Result<Unit> = registering.withLock {
        // Switched off in Settings: this phone stays unregistered
        if (!preferences.isPushEnabled()) return@withLock Result.success(Unit)
        val token = tokens.token()
        if (token == null) {
            Log.w(TAG, "No FCM token yet; registration waits for the next start or onNewToken")
            return@withLock Result.failure(PremiumException(PremiumProblem.UNKNOWN))
        }
        // Kept as "still to send" until the backend says it has it
        preferences.markPushPending(token)
        val device = deviceDetails.current()
        val request = PushTokenRequestDto(
            token = token,
            platform = IapConfig.PLATFORM,
            deviceIdentifier = preferences.installId(),
            appVersion = device.appVersion.take(APP_VERSION_MAX),
            locale = device.language.take(LOCALE_MAX),
            timezone = device.timeZone.take(TIMEZONE_MAX)
        )
        var wait = FIRST_WAIT_MILLIS
        var last: Result<Unit> = Result.failure(PremiumException(PremiumProblem.UNKNOWN))
        for (attempt in 1..ATTEMPTS) {
            last = call { who -> api.registerPushToken(who.bearer, who.externalUserId, who.deviceSecret, request) }.map { }
            if (last.isSuccess) {
                preferences.markPushRegistered()
                break
            }
            // Only "no answer" and "busy" are worth trying again now. A refused identity is not:
            // it is sorted out at the next sign-in or app start, and the registration stays pending
            val problem = last.exceptionOrNull() as? PremiumException
            val worthRetrying = problem?.problem == PremiumProblem.OFFLINE || problem?.problem == PremiumProblem.STORE_BUSY
            if (!worthRetrying || attempt == ATTEMPTS) break
            delay(maxOf(wait, problem.retryAfterSeconds * MILLIS_IN_SECOND))
            wait *= WAIT_GROWTH
        }
        if (last.isSuccess) Log.d(TAG, "Push token registered") else Log.w(TAG, "Push registration failed", last.exceptionOrNull())
        last
    }

    override suspend fun isRegistrationPending(): Boolean = preferences.isPushPending()

    override suspend fun unregister(): Result<Unit> {
        val token = preferences.lastPushToken() ?: return Result.success(Unit)
        return call { who ->
            api.unregisterPushToken(who.bearer, who.externalUserId, who.deviceSecret, PushTokenRequestDto(token = token))
        }.map { }
    }

    override suspend fun reportOpened(id: String) {
        call { who -> api.notificationOpened(who.bearer, who.externalUserId, who.deviceSecret, NotificationOpenedRequestDto(id)) }
    }

    private suspend fun <T> call(block: suspend (IapIdentity) -> T): Result<T> = caller.result {
        try {
            block(identity.current())
        } catch (e: PremiumException) {
            if (e.problem != PremiumProblem.INVALID_DEVICE_SECRET) throw e
            identity.forgetGuest()
            block(identity.current())
        }
    }

    private companion object {
        const val TAG = "IapPush"
        const val ATTEMPTS = 3
        const val FIRST_WAIT_MILLIS = 2_000L
        const val WAIT_GROWTH = 4
        const val MILLIS_IN_SECOND = 1_000L
        // The longest the backend accepts for each field
        const val APP_VERSION_MAX = 32
        const val LOCALE_MAX = 12
        const val TIMEZONE_MAX = 64
    }
}
