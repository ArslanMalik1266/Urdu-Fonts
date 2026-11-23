package com.urdufonts.app.ios

import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.repo.GoogleSignInProvider
import com.urdufonts.app.domain.repo.PushTokenSource
import com.urdufonts.app.platform.PlatformContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Google sign-in on iOS: the GoogleSignIn SDK, called from Swift through [GoogleSignInBridge]. */
class IosGoogleSignIn(private val bridge: GoogleSignInBridge) : GoogleSignInProvider {
    override suspend fun signIn(context: PlatformContext): GoogleUser = suspendCancellableCoroutine { cont ->
        com.urdufonts.app.ads.suppressNextReturnAppOpen()
        bridge.signIn { account, error ->
            if (account != null) {
                cont.resume(GoogleUser(idToken = account.idToken, email = account.email, name = account.name, profilePictureUri = account.photoUrl))
            } else {
                cont.resumeWithException(Exception(error ?: "Google sign-in was cancelled"))
            }
        }
    }
}

/** The FCM token on iOS (Firebase Messaging maps it to the APNs token), from Swift through [PushBridge]. */
class IosPushTokenSource(private val bridge: PushBridge) : PushTokenSource {
    override suspend fun token(): String? = suspendCancellableCoroutine { cont ->
        bridge.currentToken { cont.resume(it?.takeIf { token -> token.isNotBlank() }) }
    }
}
