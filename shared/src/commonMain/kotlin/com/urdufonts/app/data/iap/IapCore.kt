package com.urdufonts.app.data.iap

import com.urdufonts.app.data.remote.AppJson
import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.platform.AppBuildConfig
import com.urdufonts.app.platform.platformName
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.decodeFromString

/** Where the IAP backend lives and the app's own token for it (from local.properties / xcconfig, never in source). */
object IapConfig {
    const val BASE_URL = "https://iap.webscare.app/api/v1/"
    const val APP_SLUG = "urdufonts"
    const val GOOGLE_WEB_CLIENT_ID = "403532354068-5io7gljou5nfqpkd5kcs0o7f3gv3bmc1.apps.googleusercontent.com"

    /** "android" or "ios", as the backend names the platforms. */
    val PLATFORM: String get() = platformName.lowercase()

    val appToken: String get() = AppBuildConfig.iapAppToken
    val appBearer: String get() = "Bearer $appToken"
    val isComplete: Boolean get() = appToken.isNotBlank()
}

/** Runs a backend call and turns every way it can fail into a [PremiumException]. */
class IapCaller {

    suspend fun <T> call(block: suspend () -> T): T = try {
        if (!IapConfig.isComplete) throw PremiumException(PremiumProblem.UNKNOWN, serverMessage = "IAP_APP_TOKEN missing")
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: PremiumException) {
        throw e
    } catch (e: ResponseException) {
        val body = runCatching { e.response.bodyAsText() }.getOrNull()
        val error = body?.let { runCatching { AppJson.decodeFromString<ApiErrorDto>(it) }.getOrNull() }
        throw error.toPremiumException(e.response.status.value)
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

/** The HTTP status of a failed backend call, or null when it never got an answer. */
val Throwable.httpStatus: Int? get() = (this as? ResponseException)?.response?.status?.value

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
