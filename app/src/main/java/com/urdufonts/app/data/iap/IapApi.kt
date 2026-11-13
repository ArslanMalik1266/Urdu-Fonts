package com.urdufonts.app.data.iap

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The IAP backend (iap.webscare.app), used for sign-in, support tickets and push.
 * Every call names its own identity: the app token for sign-in, then the member token, or for a guest the
 * app token plus [IapHeaders.EXTERNAL_USER_ID] and [IapHeaders.DEVICE_SECRET] (null headers are left out).
 */
interface IapApi {

    // ── Purchases ─────────────────────────────────────────────────────────
    /** The ids Google Play should carry for the signed-in member's next purchase. */
    @GET("me/purchase-identity")
    suspend fun purchaseIdentity(@Header("Authorization") auth: String): PurchaseIdentityDto

    // ── Authentication ────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Header("Authorization") auth: String, @Body body: RegisterRequest): AuthSessionDto

    @POST("auth/login")
    suspend fun login(@Header("Authorization") auth: String, @Body body: LoginRequest): AuthSessionDto

    @POST("auth/oauth/login")
    suspend fun oauthLogin(@Header("Authorization") auth: String, @Body body: OAuthLoginRequest): AuthSessionDto

    @POST("auth/otp/request")
    suspend fun otpRequest(@Header("Authorization") auth: String, @Body body: OtpRequest): OtpRequestResponse

    @POST("auth/otp/verify")
    suspend fun otpVerify(@Header("Authorization") auth: String, @Body body: OtpVerifyRequest): OtpVerifyResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Header("Authorization") auth: String, @Body body: ResetPasswordRequest): Response<ResponseBody>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") auth: String): Response<ResponseBody>

    @POST("me/init")
    suspend fun initGuest(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Body body: GuestInitRequest
    ): GuestInitResponse

    // ── Support tickets ───────────────────────────────────────────────────
    @GET("support/tickets")
    suspend fun tickets(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): TicketListResponseDto

    @GET("support/tickets/{id}")
    suspend fun ticket(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Path("id") id: Int
    ): TicketResponseDto

    @Multipart
    @POST("support/tickets")
    suspend fun createTicket(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part attachment: MultipartBody.Part?
    ): TicketResponseDto

    @Multipart
    @POST("support/tickets/{id}/reply")
    suspend fun reply(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Path("id") id: Int,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part attachment: MultipartBody.Part?
    ): TicketReplyResponseDto

    @POST("support/tickets/{id}/resolution")
    suspend fun resolution(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Path("id") id: Int,
        @Body body: TicketResolutionRequestDto
    ): TicketResponseDto

    @POST("support/tickets/{id}/rating")
    suspend fun rating(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Path("id") id: Int,
        @Body body: TicketRatingRequestDto
    ): TicketResponseDto

    @GET("support/tickets/{id}/attachments/{attachmentId}")
    suspend fun attachment(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Path("id") id: Int,
        @Path("attachmentId") attachmentId: Int
    ): ResponseBody

    // ── Push ──────────────────────────────────────────────────────────────
    @POST("me/push-tokens")
    suspend fun registerPushToken(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Body body: PushTokenRequestDto
    ): PushTokenResponseDto

    @HTTP(method = "DELETE", path = "me/push-tokens", hasBody = true)
    suspend fun unregisterPushToken(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Body body: PushTokenRequestDto
    ): Response<ResponseBody>

    @POST("me/notifications/opened")
    suspend fun notificationOpened(
        @Header("Authorization") auth: String,
        @Header(IapHeaders.EXTERNAL_USER_ID) externalUserId: String?,
        @Header(IapHeaders.DEVICE_SECRET) deviceSecret: String?,
        @Body body: NotificationOpenedRequestDto
    ): Response<ResponseBody>
}

object IapHeaders {
    const val EXTERNAL_USER_ID = "X-External-User-Id"
    const val DEVICE_SECRET = "X-Device-Secret"
}
