package com.webscare.urdufonts.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.webscare.urdufonts.data.local.UserPreferences
import com.webscare.urdufonts.data.remote.api.AuthApiService
import com.webscare.urdufonts.data.remote.dto.GoogleLoginRequestDto
import com.webscare.urdufonts.data.mapper.toDto
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.AuthResult
import com.webscare.urdufonts.domain.models.GoogleUser
import com.webscare.urdufonts.domain.models.UserSession
import com.webscare.urdufonts.domain.repo.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun registerUser(
        params: RegisterParams
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        runCatching {
            val requestMap = params.toDto().toFieldMap()
            apiService.registerUser(requestMap).toDomain()
        }
    }

    override suspend fun signInWithGoogle(
        context: Context
    ): Result<GoogleUser> = withContext(Dispatchers.IO) {
        runCatching {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId("463838559120-ffqd6ja9j2grijftslonqipcs8ptha3q.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                android.util.Log.i("GoogleSignIn", "=== GOOGLE USER DETAILS ===")
                android.util.Log.i("GoogleSignIn", "Email: ${googleIdTokenCredential.id}")
                android.util.Log.i("GoogleSignIn", "Display Name: ${googleIdTokenCredential.displayName}")
                android.util.Log.i("GoogleSignIn", "Profile Picture URI: ${googleIdTokenCredential.profilePictureUri}")
                android.util.Log.i("GoogleSignIn", "ID Token: ${googleIdTokenCredential.idToken}")
                android.util.Log.i("GoogleSignIn", "===========================")

                GoogleUser(
                    idToken = googleIdTokenCredential.idToken,
                    email = googleIdTokenCredential.id,
                    name = googleIdTokenCredential.displayName,
                    profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                throw Exception("Unexpected credential type: ${credential.type}")
            }
        }.onFailure { exception ->
            android.util.Log.e("GoogleSignIn", "Repository: Google One Tap Sign-In Failed!", exception)
            android.util.Log.e("GoogleSignIn", "Repository: Exception class: ${exception.javaClass.name}")
            android.util.Log.e("GoogleSignIn", "Repository: Exception message: ${exception.message}")
            logAppIdentityDetails(context, "463838559120-ffqd6ja9j2grijftslonqipcs8ptha3q.apps.googleusercontent.com")
        }
    }

    private fun logAppIdentityDetails(context: Context, clientId: String) {
        android.util.Log.i("GoogleSignIn", "=== APP IDENTITY CONFIGURATION CHECK ===")
        android.util.Log.i("GoogleSignIn", "Running Package Name: ${context.packageName}")
        android.util.Log.i("GoogleSignIn", "Using Client ID in Code: $clientId")
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val md = MessageDigest.getInstance("SHA-1")
                val publicKey = md.digest(signatures[0].toByteArray())
                val hexString = StringBuilder()
                for (i in publicKey.indices) {
                    val appendString = Integer.toHexString(0xFF and publicKey[i].toInt())
                    if (appendString.length == 1) hexString.append("0")
                    hexString.append(appendString)
                }
                val sha1 = hexString.toString().uppercase().chunked(2).joinToString(":")
                android.util.Log.i("GoogleSignIn", "Running App debug/release SHA-1: $sha1")
            } else {
                android.util.Log.e("GoogleSignIn", "No app signatures found!")
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Error getting signature: ${e.message}", e)
        }
        android.util.Log.i("GoogleSignIn", "========================================")
    }

    override suspend fun loginWithGoogle(
        idToken: String
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        runCatching {
            val request = GoogleLoginRequestDto(idToken)
            apiService.googleLogin(request).toDomain()
        }
    }

    override fun getSession(): Flow<UserSession?> = userPreferences.userSession

    override suspend fun saveSession(session: UserSession) {
        userPreferences.saveUserSession(session)
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            userPreferences.clearUserSession()
        }
    }
}
