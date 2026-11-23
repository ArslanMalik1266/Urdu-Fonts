package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.platform.PlatformContext

/**
 * Shows Google's own account picker and returns the chosen account's ID token.
 * Android: Credential Manager. iOS: the GoogleSignIn SDK (implemented in Swift, see IosBridges).
 */
interface GoogleSignInProvider {
    suspend fun signIn(context: PlatformContext): GoogleUser
}
