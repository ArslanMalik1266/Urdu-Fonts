package com.urdufonts.app.data.iap

import com.google.gson.annotations.SerializedName

// ── Errors ────────────────────────────────────────────────────────────────
data class ApiErrorDto(
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("retry_after") val retryAfter: Int? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
)

// ── Authentication ────────────────────────────────────────────────────────
data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("platform") val platform: String = "android"
)

data class LoginRequest(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String
)

data class OAuthLoginRequest(
    @SerializedName("provider") val provider: String,
    @SerializedName("id_token") val idToken: String,
    @SerializedName("platform") val platform: String = "android"
)

data class OtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("purpose") val purpose: String
)

data class OtpRequestResponse(
    @SerializedName("code_length") val codeLength: Int? = null,
    @SerializedName("resend_available_in") val resendAvailableIn: Int? = null
)

data class OtpVerifyRequest(
    @SerializedName("email") val email: String,
    @SerializedName("purpose") val purpose: String,
    @SerializedName("code") val code: String,
    @SerializedName("platform") val platform: String = "android"
)

data class OtpVerifyResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("reset_token") val resetToken: String? = null,
    @SerializedName("member") val member: MemberDto? = null
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String,
    @SerializedName("password") val password: String
)

data class AuthSessionDto(
    @SerializedName("token") val token: String? = null,
    @SerializedName("member") val member: MemberDto? = null
)

data class MemberDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("external_user_id") val externalUserId: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email_verified") val emailVerified: Boolean? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class GuestInitRequest(
    @SerializedName("external_user_id") val externalUserId: String?,
    @SerializedName("platform") val platform: String = "android"
)

data class GuestInitResponse(
    @SerializedName("external_user_id") val externalUserId: String? = null,
    @SerializedName("device_secret") val deviceSecret: String? = null
)

// ── Support tickets ───────────────────────────────────────────────────────
data class TicketDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("category") val category: TicketCategoryDto? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("messages") val messages: List<TicketMessageDto>? = null,
    @SerializedName("latest_message") val latestMessage: TicketMessageDto? = null,
    @SerializedName("resolution") val resolution: TicketResolutionDto? = null,
    @SerializedName("rating") val rating: TicketRatingDto? = null,
    @SerializedName("can_rate") val canRate: Boolean? = null
)

data class TicketCategoryDto(@SerializedName("name") val name: String? = null)

data class TicketMessageDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("author_type") val authorType: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("attachments") val attachments: List<TicketAttachmentDto>? = null
)

data class TicketAttachmentDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("filename") val filename: String? = null,
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("size_bytes") val sizeBytes: Long? = null
)

data class TicketResolutionDto(
    @SerializedName("awaiting_confirmation") val awaitingConfirmation: Boolean? = null,
    @SerializedName("auto_close_at") val autoCloseAt: String? = null
)

data class TicketRatingDto(@SerializedName("stars") val stars: Int? = null)

data class TicketResponseDto(
    @SerializedName("ticket") val ticket: TicketDto? = null,
    @SerializedName("device_secret") val deviceSecret: String? = null
)

data class TicketListResponseDto(
    @SerializedName("tickets") val tickets: List<TicketDto>? = null,
    @SerializedName("pagination") val pagination: TicketPaginationDto? = null
)

data class TicketPaginationDto(@SerializedName("total_pages") val totalPages: Int? = null)

data class TicketReplyResponseDto(
    @SerializedName("ticket_status") val ticketStatus: String? = null,
    @SerializedName("message") val message: TicketMessageDto? = null
)

data class TicketResolutionRequestDto(
    @SerializedName("solved") val solved: Boolean,
    @SerializedName("reason") val reason: String? = null
)

data class TicketRatingRequestDto(
    @SerializedName("stars") val stars: Int,
    @SerializedName("comment") val comment: String? = null
)

// ── Push ──────────────────────────────────────────────────────────────────
data class PushTokenRequestDto(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("device_identifier") val deviceIdentifier: String? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("locale") val locale: String? = null,
    @SerializedName("timezone") val timezone: String? = null
)

data class PushTokenResponseDto(@SerializedName("registered") val registered: Boolean? = null)

data class NotificationOpenedRequestDto(@SerializedName("nid") val nid: String)

data class PurchaseIdentityDto(
    @com.google.gson.annotations.SerializedName("external_user_id") val externalUserId: String? = null,
    @com.google.gson.annotations.SerializedName("obfuscated_account_id") val obfuscatedAccountId: String? = null
)
