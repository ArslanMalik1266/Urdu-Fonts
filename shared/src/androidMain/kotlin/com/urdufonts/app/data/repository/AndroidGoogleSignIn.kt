package com.urdufonts.app.data.repository

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.urdufonts.app.ads.suppressNextReturnAppOpen
import com.urdufonts.app.data.iap.IapConfig
import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.repo.GoogleSignInProvider
import com.urdufonts.app.platform.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Google sign-in through Android's Credential Manager. */
class AndroidGoogleSignIn : GoogleSignInProvider {
    override suspend fun signIn(context: PlatformContext): GoogleUser = withContext(Dispatchers.IO) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(IapConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        suppressNextReturnAppOpen()
        val credential = CredentialManager.create(context).getCredential(context = context, request = request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            GoogleUser(
                idToken = google.idToken,
                email = google.id,
                name = google.displayName,
                profilePictureUri = google.profilePictureUri?.toString()
            )
        } else {
            throw Exception("Unexpected credential type: ${credential.type}")
        }
    }
}
