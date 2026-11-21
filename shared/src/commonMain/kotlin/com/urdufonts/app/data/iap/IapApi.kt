package com.urdufonts.app.data.iap

import com.urdufonts.app.domain.models.AttachmentDraft
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

/**
 * The IAP backend (iap.webscare.app), used for sign-in, support tickets and push.
 * Every call names its own identity: the app token for sign-in, then the member token, or for a guest the
 * app token plus [IapHeaders.EXTERNAL_USER_ID] and [IapHeaders.DEVICE_SECRET] (null headers are left out).
 * Calls throw a Ktor `ResponseException` on a non-2xx answer unless marked otherwise.
 */
class IapApi(private val client: HttpClient) {

    // ── Purchases ──────────────────────────────────────────────────────────
    /** The ids Google Play should carry for the signed-in member's next purchase. */
    suspend fun purchaseIdentity(auth: String): PurchaseIdentityDto =
        client.get("me/purchase-identity") { header("Authorization", auth) }.body()

    // ── Authentication ─────────────────────────────────────────────────────
    suspend fun register(auth: String, body: RegisterRequest): AuthSessionDto = postJson("auth/register", auth, body)

    suspend fun login(auth: String, body: LoginRequest): AuthSessionDto = postJson("auth/login", auth, body)

    suspend fun oauthLogin(auth: String, body: OAuthLoginRequest): AuthSessionDto = postJson("auth/oauth/login", auth, body)

    suspend fun otpRequest(auth: String, body: OtpRequest): OtpRequestResponse = postJson("auth/otp/request", auth, body)

    suspend fun otpVerify(auth: String, body: OtpVerifyRequest): OtpVerifyResponse = postJson("auth/otp/verify", auth, body)

    suspend fun resetPassword(auth: String, body: ResetPasswordRequest) {
        client.post("auth/reset-password") { header("Authorization", auth); json(body) }
    }

    /** Never fails on the backend's answer: the local sign-out goes ahead either way. */
    suspend fun logout(auth: String) {
        client.post("auth/logout") { header("Authorization", auth); expectSuccess = false }
    }

    suspend fun initGuest(auth: String, deviceSecret: String?, body: GuestInitRequest): GuestInitResponse =
        client.post("me/init") {
            header("Authorization", auth)
            header(IapHeaders.DEVICE_SECRET, deviceSecret)
            json(body)
        }.body()

    // ── Support tickets ────────────────────────────────────────────────────
    suspend fun tickets(auth: String, externalUserId: String?, deviceSecret: String?, page: Int, perPage: Int): TicketListResponseDto =
        client.get("support/tickets") {
            identity(auth, externalUserId, deviceSecret)
            parameter("page", page)
            parameter("per_page", perPage)
        }.body()

    suspend fun ticket(auth: String, externalUserId: String?, deviceSecret: String?, id: Int): TicketResponseDto =
        client.get("support/tickets/$id") { identity(auth, externalUserId, deviceSecret) }.body()

    suspend fun createTicket(
        auth: String,
        externalUserId: String?,
        deviceSecret: String?,
        fields: Map<String, String>,
        attachment: AttachmentDraft?
    ): TicketResponseDto = multipart("support/tickets", auth, externalUserId, deviceSecret, fields, attachment).body()

    suspend fun reply(
        auth: String,
        externalUserId: String?,
        deviceSecret: String?,
        id: Int,
        fields: Map<String, String>,
        attachment: AttachmentDraft?
    ): TicketReplyResponseDto = multipart("support/tickets/$id/reply", auth, externalUserId, deviceSecret, fields, attachment).body()

    suspend fun resolution(auth: String, externalUserId: String?, deviceSecret: String?, id: Int, body: TicketResolutionRequestDto): TicketResponseDto =
        client.post("support/tickets/$id/resolution") { identity(auth, externalUserId, deviceSecret); json(body) }.body()

    suspend fun rating(auth: String, externalUserId: String?, deviceSecret: String?, id: Int, body: TicketRatingRequestDto): TicketResponseDto =
        client.post("support/tickets/$id/rating") { identity(auth, externalUserId, deviceSecret); json(body) }.body()

    suspend fun attachment(auth: String, externalUserId: String?, deviceSecret: String?, id: Int, attachmentId: Int): ByteArray =
        client.get("support/tickets/$id/attachments/$attachmentId") { identity(auth, externalUserId, deviceSecret) }.body()

    // ── Push ───────────────────────────────────────────────────────────────
    suspend fun registerPushToken(auth: String, externalUserId: String?, deviceSecret: String?, body: PushTokenRequestDto): PushTokenResponseDto =
        client.post("me/push-tokens") { identity(auth, externalUserId, deviceSecret); json(body) }.body()

    /** Like the Retrofit version, an error answer here is not thrown. */
    suspend fun unregisterPushToken(auth: String, externalUserId: String?, deviceSecret: String?, body: PushTokenRequestDto) {
        client.delete("me/push-tokens") { identity(auth, externalUserId, deviceSecret); json(body); expectSuccess = false }
    }

    /** Like the Retrofit version, an error answer here is not thrown. */
    suspend fun notificationOpened(auth: String, externalUserId: String?, deviceSecret: String?, body: NotificationOpenedRequestDto) {
        client.post("me/notifications/opened") { identity(auth, externalUserId, deviceSecret); json(body); expectSuccess = false }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private suspend inline fun <reified T, reified R> postJson(path: String, auth: String, body: R): T =
        client.post(path) { header("Authorization", auth); json(body) }.body()

    private inline fun <reified R> HttpRequestBuilder.json(body: R) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private fun HttpRequestBuilder.identity(auth: String, externalUserId: String?, deviceSecret: String?) {
        header("Authorization", auth)
        header(IapHeaders.EXTERNAL_USER_ID, externalUserId)
        header(IapHeaders.DEVICE_SECRET, deviceSecret)
    }

    private suspend fun multipart(
        path: String,
        auth: String,
        externalUserId: String?,
        deviceSecret: String?,
        fields: Map<String, String>,
        attachment: AttachmentDraft?
    ) = client.submitFormWithBinaryData(
        url = path,
        formData = formData {
            fields.forEach { (name, value) -> append(name, value) }
            if (attachment != null) {
                append("attachment", attachment.bytes, Headers.build {
                    append(HttpHeaders.ContentType, attachment.mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"${attachment.fileName.replace("\"", "")}\"")
                })
            }
        }
    ) { identity(auth, externalUserId, deviceSecret) }
}

object IapHeaders {
    const val EXTERNAL_USER_ID = "X-External-User-Id"
    const val DEVICE_SECRET = "X-Device-Secret"
}
