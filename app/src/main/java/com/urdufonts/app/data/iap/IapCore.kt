package com.urdufonts.app.data.iap

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.urdufonts.app.BuildConfig
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

/** Where the IAP backend lives and the app's own token for it (from local.properties, never in source). */
object IapConfig {
    const val BASE_URL = "https://iap.webscare.app/api/v1/"
    const val APP_SLUG = "urdufonts"
    const val GOOGLE_WEB_CLIENT_ID = "403532354068-5io7gljou5nfqpkd5kcs0o7f3gv3bmc1.apps.googleusercontent.com"
    const val PLATFORM = "android"

    val appToken: String get() = BuildConfig.IAP_APP_TOKEN
    val appBearer: String get() = "Bearer $appToken"
    val isComplete: Boolean get() = appToken.isNotBlank()
}

/** Runs a backend call and turns every way it can fail into a [PremiumException]. */
class IapCaller(private val gson: Gson) {

    suspend fun <T> call(block: suspend () -> T): T = try {
        if (!IapConfig.isComplete) throw PremiumException(PremiumProblem.UNKNOWN, serverMessage = "IAP_APP_TOKEN missing")
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: PremiumException) {
        throw e
    } catch (e: HttpException) {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val error = runCatching { gson.fromJson(body, ApiErrorDto::class.java) }.getOrNull()
        throw error.toPremiumException(e.code())
    } catch (e: IOException) {
        throw PremiumException(PremiumProblem.OFFLINE)
    } catch (e: Exception) {
        throw PremiumException(PremiumProblem.UNKNOWN, serverMessage = e.message)
    }

    /** Like [call], but answers a [Result] instead of throwing. */
    suspend fun <T> result(block: suspend () -> T): Result<T> = try {
        Result.success(call(block))
    } catch (e: CancellationException) {
        throw e
    } catch (e: PremiumException) {
        Result.failure(e)
    }
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_CONFLICT = 409
private const val HTTP_TOO_MANY = 429
private const val HTTP_UNAVAILABLE = 503

/** A refusal from the backend (its HTTP status and `error` code) as the app's own problem. */
fun ApiErrorDto?.toPremiumException(status: Int): PremiumException {
    val code = this?.error.orEmpty()
    val problem = when (code) {
        "store_unavailable", "purchase_ingestion_paused" -> PremiumProblem.STORE_BUSY
        "invalid_device_secret" -> PremiumProblem.INVALID_DEVICE_SECRET
        "not_awaiting_confirmation", "not_closed", "rating_locked" -> PremiumProblem.TICKET_CHANGED
        "oauth_verification_failed", "oauth_disabled", "login_disabled", "account_exists", "invalid_credentials" ->
            PremiumProblem.SIGN_IN_REJECTED
        "authentication_required", "member_authentication_required", "identity_verification_required" ->
            PremiumProblem.SESSION_EXPIRED
        else -> when (status) {
            HTTP_UNAUTHORIZED -> PremiumProblem.SESSION_EXPIRED
            HTTP_CONFLICT -> PremiumProblem.UNKNOWN
            HTTP_TOO_MANY, HTTP_UNAVAILABLE -> PremiumProblem.STORE_BUSY
            else -> PremiumProblem.UNKNOWN
        }
    }
    // A validation failure carries its sentences per field; the first one is the most useful to show
    val message = this?.errors?.values?.firstOrNull()?.firstOrNull() ?: this?.message
    return PremiumException(problem, retryAfterSeconds = this?.retryAfter ?: 0, serverMessage = message, code = code.ifBlank { null })
}

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

/** Who this install is to the backend while nobody is signed in. */
data class GuestIdentity(val externalUserId: String, val deviceSecret: String)

/**
 * Who a support or push call is made as. A signed-in member sends their own token and nothing else; a
 * guest sends the app's token with their id and the device secret that proves the id is theirs.
 */
data class IapIdentity(
    val bearer: String,
    val externalUserId: String? = null,
    val deviceSecret: String? = null
)

/** Session role that marks a login made with the IAP backend (older dashboard logins are signed out once). */
const val IAP_SESSION_ROLE = "iap_member"

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
