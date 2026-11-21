package com.urdufonts.app.data.iap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// ── Errors ────────────────────────────────────────────────────────────────
@Serializable
data class ApiErrorDto(
    @SerialName("error") val error: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("retry_after") val retryAfter: Int? = null,
    @SerialName("errors") val errors: Map<String, List<String>>? = null
)

// ── Authentication ────────────────────────────────────────────────────────
@Serializable
data class RegisterRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("platform") val platform: String = IapConfig.PLATFORM
)

@Serializable
data class LoginRequest(
    @SerialName("identifier") val identifier: String,
    @SerialName("password") val password: String
)

@Serializable
data class OAuthLoginRequest(
    @SerialName("provider") val provider: String,
    @SerialName("id_token") val idToken: String,
    @SerialName("platform") val platform: String = IapConfig.PLATFORM
)

@Serializable
data class OtpRequest(
    @SerialName("email") val email: String,
    @SerialName("purpose") val purpose: String
)

@Serializable
data class OtpRequestResponse(
    @SerialName("code_length") val codeLength: Int? = null,
    @SerialName("resend_available_in") val resendAvailableIn: Int? = null
)

@Serializable
data class OtpVerifyRequest(
    @SerialName("email") val email: String,
    @SerialName("purpose") val purpose: String,
    @SerialName("code") val code: String,
    @SerialName("platform") val platform: String = IapConfig.PLATFORM
)

@Serializable
data class OtpVerifyResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("reset_token") val resetToken: String? = null,
    @SerialName("member") val member: MemberDto? = null
)

@Serializable
data class ResetPasswordRequest(
    @SerialName("email") val email: String,
    @SerialName("token") val token: String,
    @SerialName("password") val password: String
)

@Serializable
data class AuthSessionDto(
    @SerialName("token") val token: String? = null,
    @SerialName("member") val member: MemberDto? = null
)

@Serializable
data class MemberDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("external_user_id") val externalUserId: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class GuestInitRequest(
    @SerialName("external_user_id") val externalUserId: String?,
    @SerialName("platform") val platform: String = IapConfig.PLATFORM
)

@Serializable
data class GuestInitResponse(
    @SerialName("external_user_id") val externalUserId: String? = null,
    @SerialName("device_secret") val deviceSecret: String? = null
)

// ── Support tickets ───────────────────────────────────────────────────────
@Serializable
data class TicketDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("subject") val subject: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("category") val category: TicketCategoryDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("messages") val messages: List<TicketMessageDto>? = null,
    @SerialName("latest_message") val latestMessage: TicketMessageDto? = null,
    @SerialName("resolution") val resolution: TicketResolutionDto? = null,
    @SerialName("rating") val rating: TicketRatingDto? = null,
    @SerialName("can_rate") val canRate: Boolean? = null
)

@Serializable
data class TicketCategoryDto(@SerialName("name") val name: String? = null)

@Serializable
data class TicketMessageDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("author_type") val authorType: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("attachments") val attachments: List<TicketAttachmentDto>? = null
)

@Serializable
data class TicketAttachmentDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("size_bytes") val sizeBytes: Long? = null
)

@Serializable
data class TicketResolutionDto(
    @SerialName("awaiting_confirmation") val awaitingConfirmation: Boolean? = null,
    @SerialName("auto_close_at") val autoCloseAt: String? = null
)

@Serializable
data class TicketRatingDto(@SerialName("stars") val stars: Int? = null)

@Serializable
data class TicketResponseDto(
    @SerialName("ticket") val ticket: TicketDto? = null,
    @SerialName("device_secret") val deviceSecret: String? = null
)

@Serializable
data class TicketListResponseDto(
    @SerialName("tickets") val tickets: List<TicketDto>? = null,
    @SerialName("pagination") val pagination: TicketPaginationDto? = null
)

@Serializable
data class TicketPaginationDto(@SerialName("total_pages") val totalPages: Int? = null)

@Serializable
data class TicketReplyResponseDto(
    @SerialName("ticket_status") val ticketStatus: String? = null,
    @SerialName("message") val message: TicketMessageDto? = null
)

@Serializable
data class TicketResolutionRequestDto(
    @SerialName("solved") val solved: Boolean,
    @SerialName("reason") val reason: String? = null
)

@Serializable
data class TicketRatingRequestDto(
    @SerialName("stars") val stars: Int,
    @SerialName("comment") val comment: String? = null
)

// ── Push ──────────────────────────────────────────────────────────────────
@Serializable
data class PushTokenRequestDto(
    @SerialName("token") val token: String,
    @SerialName("platform") val platform: String? = null,
    @SerialName("device_identifier") val deviceIdentifier: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("locale") val locale: String? = null,
    @SerialName("timezone") val timezone: String? = null
)

@Serializable
data class PushTokenResponseDto(@SerialName("registered") val registered: Boolean? = null)

@Serializable
data class NotificationOpenedRequestDto(@SerialName("nid") val nid: String)

@Serializable
data class PurchaseIdentityDto(
    @SerialName("external_user_id") val externalUserId: String? = null,
    @SerialName("obfuscated_account_id") val obfuscatedAccountId: String? = null
)
