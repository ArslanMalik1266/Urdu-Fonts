package com.urdufonts.app.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.data.remote.api.AuthApiService
import com.urdufonts.app.data.remote.dto.GoogleLoginRequestDto
import com.urdufonts.app.data.mapper.toDto
import com.urdufonts.app.data.mapper.toDomain
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.models.UserSession
import com.urdufonts.app.domain.repo.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

import com.urdufonts.app.data.local.EntitlementLocalDataSource

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val userPreferences: UserPreferences,
    private val entitlementLocalDataSource: EntitlementLocalDataSource? = null
) : AuthRepository {

    override suspend fun registerUser(
        params: RegisterParams
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerUser(
                name = params.name,
                email = params.email,
                pass = params.pass
            ).toDomain()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e.extractErrorMessage())
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

            com.webscare.ads.WebsCareAds.suppressNextReturnAppOpen()
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
        try {
            val request = GoogleLoginRequestDto(idToken)
            val session = apiService.googleLogin(request).toDomain()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e.extractErrorMessage())
        }
    }

    override fun getSession(): Flow<UserSession?> = userPreferences.userSession

    override suspend fun saveSession(session: UserSession) {
        userPreferences.saveUserSession(session)
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            userPreferences.clearUserSession()
            userPreferences.saveSubscriptionStatus(isPro = false, planId = null, purchaseToken = null)
            entitlementLocalDataSource?.clearEntitlements()
            Unit
        }
    }

    override suspend fun checkUserExists(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            apiService.checkUser(email)
            Result.success(true)
        } catch (e: Exception) {
            if (e is retrofit2.HttpException && e.code() == 404) {
                Result.success(false)
            } else {
                Result.failure(e.extractErrorMessage())
            }
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(email = email, otp = otp)
            val session = response.toDomain()
            saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e.extractErrorMessage())
        }
    }

    override suspend fun login(email: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(email = email, pass = password)
            val session = response.toDomain()
            saveSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e.extractErrorMessage())
        }
    }

    private fun Throwable.extractErrorMessage(): Exception {
        if (this is retrofit2.HttpException) {
            try {
                val errorJson = response()?.errorBody()?.string()
                if (!errorJson.isNullOrBlank()) {
                    val jsonObject = com.google.gson.JsonParser.parseString(errorJson).asJsonObject
                    val message = jsonObject.get("message")?.asString
                        ?: jsonObject.get("error")?.asString
                        ?: jsonObject.get("msg")?.asString
                    if (!message.isNullOrBlank()) {
                        return Exception(message)
                    }
                }
            } catch (_: Exception) {}
        }
        return if (this is Exception) this else Exception(message ?: "An unexpected error occurred.")
    }
}
