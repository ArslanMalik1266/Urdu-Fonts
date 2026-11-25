package com.urdufonts.app.domain.repo

/** What Apple's sign-in sheet returns. [email] and [name] come only on the first sign-in of this Apple ID. */
data class AppleUser(val idToken: String, val email: String?, val name: String?)

/** Sign in with Apple. Only iOS provides one; on Android none is registered and the button is hidden. */
interface AppleSignInProvider {
    suspend fun signIn(): AppleUser
}
