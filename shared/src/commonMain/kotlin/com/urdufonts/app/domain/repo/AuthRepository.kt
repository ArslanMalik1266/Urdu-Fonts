package com.urdufonts.app.domain.repo

import com.urdufonts.app.platform.PlatformContext
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.models.UserSession
import kotlinx.coroutines.flow.Flow

/** Sign-in with the IAP backend (iap.webscare.app): email + password, email codes and Google. */
interface AuthRepository {
    /** Creates the account and emails a verification code; the account is signed in once the code is verified. */
    suspend fun registerUser(params: RegisterParams): Result<AuthResult>
    suspend fun signInWithGoogle(context: PlatformContext): Result<GoogleUser>
    suspend fun loginWithGoogle(idToken: String): Result<UserSession>

    /** True where Sign in with Apple exists (iOS). */
    val isAppleSignInAvailable: Boolean get() = false

    /** Apple's sign-in sheet, then the IAP backend (auth/oauth/login, provider "apple"). */
    suspend fun signInWithApple(): Result<UserSession> = Result.failure(UnsupportedOperationException("Sign in with Apple is not available"))
    fun getSession(): Flow<UserSession?>
    suspend fun saveSession(session: UserSession)
    suspend fun logout(): Result<Unit>
    suspend fun checkUserExists(email: String): Result<Boolean>

    /** Verifies the code from the sign-up email and signs the new account in. */
    suspend fun verifyOtp(email: String, otp: String): Result<UserSession>
    suspend fun login(email: String, password: String): Result<UserSession>

    /** Emails a new sign-up verification code. */
    suspend fun resendVerificationCode(email: String): Result<Unit>

    /** Emails a password-reset code (always succeeds for a well-formed address, by design). */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /** Checks the password-reset code and returns the reset token for [resetPassword]. */
    suspend fun verifyPasswordResetCode(email: String, code: String): Result<String>

    /** Sets the new password; every other session of the account is signed out. */
    suspend fun resetPassword(email: String, resetToken: String, newPassword: String): Result<Unit>

    /** Signs out a login made with the old urdufonts.com dashboard, once, so the user signs in again with IAP. */
    suspend fun dropLegacySession()
}
