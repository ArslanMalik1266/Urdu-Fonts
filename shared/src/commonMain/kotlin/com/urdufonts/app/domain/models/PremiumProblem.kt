package com.urdufonts.app.domain.models

/** Every way signing in, buying, support or push can go wrong. Each gets its own sentence on screen. */
enum class PremiumProblem {
    /** No internet, or the backend did not answer in time. */
    OFFLINE,

    /** The saved login is no longer accepted; the customer has to sign in again. */
    SESSION_EXPIRED,

    /** Google sign-in cannot run on this phone or build. */
    SIGN_IN_UNAVAILABLE,

    /** Google or the backend refused the sign-in. */
    SIGN_IN_REJECTED,

    /** The backend could not reach Google Play right now (or is busy). The purchase is kept and tried again. */
    STORE_BUSY,

    /** Google Play billing is missing or switched off on this phone, or the store has not sent the plans. */
    BILLING_UNAVAILABLE,

    /** This Google Play purchase already belongs to a different account. */
    OWNED_BY_ANOTHER_ACCOUNT,

    /** The running subscription is not on this phone's Google Play account, so it cannot be changed here. */
    SUBSCRIPTION_NOT_ON_PHONE,

    /** The saved guest secret is not the one the backend holds for that guest. */
    INVALID_DEVICE_SECRET,

    /** A support ticket changed on the backend meanwhile (staff reopened or closed it): it is loaded again. */
    TICKET_CHANGED,

    /** Google did not accept the purchase as paid. */
    PURCHASE_REJECTED,

    UNKNOWN,
}

/** Carries a [PremiumProblem] out of the data layer. [retryAfterSeconds] is set when the backend names a wait. */
class PremiumException(
    val problem: PremiumProblem,
    val retryAfterSeconds: Int = 0,
    /** The backend's own sentence, when it sent one (e.g. "These credentials do not match our records."). */
    val serverMessage: String? = null,
    /** The backend's error code, such as `invalid_otp`, when it sent one. */
    val code: String? = null,
) : Exception(serverMessage ?: problem.name)

fun Throwable.toPremiumProblem(): PremiumProblem = (this as? PremiumException)?.problem ?: PremiumProblem.UNKNOWN
