package com.urdufonts.app.data.iap

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

private val Context.iapDataStore: DataStore<Preferences> by preferencesDataStore(name = "iap_preferences")

/** This install's guest identity and push state, kept in the app's private DataStore. */
class IapPreferences(private val context: Context) {
    private val store get() = context.iapDataStore

    val guest: Flow<GuestIdentity?> = store.data.map { prefs ->
        val id = prefs[GUEST_ID]
        val secret = prefs[GUEST_SECRET]
        if (id.isNullOrBlank() || secret.isNullOrBlank()) null else GuestIdentity(id, secret)
    }

    suspend fun saveGuest(guest: GuestIdentity) = store.edit {
        it[GUEST_ID] = guest.externalUserId
        it[GUEST_SECRET] = guest.deviceSecret
    }

    suspend fun clearGuest() = store.edit {
        it.remove(GUEST_ID)
        it.remove(GUEST_SECRET)
    }

    val pushEnabled: Flow<Boolean> = store.data.map { it[PUSH_ENABLED] ?: true }
    suspend fun isPushEnabled(): Boolean = pushEnabled.first()
    suspend fun setPushEnabled(on: Boolean) = store.edit { it[PUSH_ENABLED] = on }

    suspend fun installId(): String {
        store.data.first()[INSTALL_ID]?.let { return it }
        val fresh = UUID.randomUUID().toString()
        store.edit { if (it[INSTALL_ID] == null) it[INSTALL_ID] = fresh }
        return store.data.first()[INSTALL_ID] ?: fresh
    }

    suspend fun lastPushToken(): String? = store.data.first()[LAST_PUSH_TOKEN]
    suspend fun markPushPending(token: String) = store.edit {
        it[LAST_PUSH_TOKEN] = token
        it[PUSH_PENDING] = true
    }
    suspend fun markPushRegistered() = store.edit { it[PUSH_PENDING] = false }
    suspend fun isPushPending(): Boolean = store.data.first()[PUSH_PENDING] ?: false

    private companion object {
        val GUEST_ID = stringPreferencesKey("guest_external_user_id")
        val GUEST_SECRET = stringPreferencesKey("guest_device_secret")
        val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        val INSTALL_ID = stringPreferencesKey("install_id")
        val LAST_PUSH_TOKEN = stringPreferencesKey("last_push_token")
        val PUSH_PENDING = booleanPreferencesKey("push_pending")
    }
}

/**
 * Works out who a support or push call is made as: the signed-in member when there is one, otherwise this
 * install's guest. A guest identity that was never set up is set up now.
 */
class IapIdentityProvider(
    private val api: IapApi,
    private val caller: IapCaller,
    private val userPreferences: UserPreferences,
    private val preferences: IapPreferences
) {
    private val guestLock = Mutex()

    suspend fun memberToken(): String? = userPreferences.userSession.firstOrNull()
        ?.takeIf { it.role == IAP_SESSION_ROLE && it.token.isNotBlank() }?.token

    suspend fun current(): IapIdentity {
        memberToken()?.let { return IapIdentity(bearer = "Bearer $it") }
        val guest = guest()
        return IapIdentity(IapConfig.appBearer, guest.externalUserId, guest.deviceSecret)
    }

    /** The saved guest, or a new one the backend creates now. */
    suspend fun guest(): GuestIdentity = guestLock.withLock {
        preferences.guest.first()?.let { return@withLock it }
        val answer = caller.call { api.initGuest(IapConfig.appBearer, null, GuestInitRequest(externalUserId = null)) }
        val id = answer.externalUserId
        val secret = answer.deviceSecret
        if (id.isNullOrBlank() || secret.isNullOrBlank()) throw PremiumException(PremiumProblem.UNKNOWN)
        GuestIdentity(id, secret).also { preferences.saveGuest(it) }
    }

    /** The backend no longer accepts the saved guest: forget it, so the next call makes a new one. */
    suspend fun forgetGuest() = preferences.clearGuest()
}
