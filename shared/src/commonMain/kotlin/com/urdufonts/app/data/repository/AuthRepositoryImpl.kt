package com.urdufonts.app.data.repository

import com.urdufonts.app.platform.PlatformContext
import com.urdufonts.app.platform.AppLog as Log
import com.urdufonts.app.data.iap.AuthSessionDto
import com.urdufonts.app.data.iap.IAP_SESSION_ROLE
import com.urdufonts.app.data.iap.IapApi
import com.urdufonts.app.data.iap.IapCaller
import com.urdufonts.app.data.iap.IapConfig
import com.urdufonts.app.data.iap.LoginRequest
import com.urdufonts.app.data.iap.MemberDto
import com.urdufonts.app.data.iap.OAuthLoginRequest
import com.urdufonts.app.data.iap.OtpRequest
import com.urdufonts.app.data.iap.OtpVerifyRequest
import com.urdufonts.app.data.iap.RegisterRequest
import com.urdufonts.app.data.iap.ResetPasswordRequest
import com.urdufonts.app.data.local.EntitlementLocalDataSource
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.models.PremiumException
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.User
import com.urdufonts.app.domain.models.UserSession
import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.repo.PushRepository
import com.urdufonts.app.domain.repo.GoogleSignInProvider
import kotlinx.coroutines.IO
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Accounts live on the IAP backend (iap.webscare.app). A successful sign-in saves the member token as the
 * [UserSession] token (role [IAP_SESSION_ROLE]); support tickets and push then go out as that member.
 */
class AuthRepositoryImpl(
    private val api: IapApi,
    private val caller: IapCaller,
    private val userPreferences: UserPreferences,
    private val push: PushRepository,
    private val googleSignIn: GoogleSignInProvider,
    private val appleSignIn: com.urdufonts.app.domain.repo.AppleSignInProvider? = null,
    private val entitlementLocalDataSource: EntitlementLocalDataSource? = null
) : AuthRepository {

    // Push registration outlives the screen that signed in
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Registration answers with a session at once; it is kept here until the emailed code is verified. */
    private var pendingRegistration: Pair<String, UserSession>? = null

    override suspend fun registerUser(params: RegisterParams): Result<AuthResult> = caller.result {
        val email = params.email.trim()
        val answer = api.register(IapConfig.appBearer, RegisterRequest(email = email, password = params.pass))
        val session = answer.toSession(fallbackName = params.name.trim())
        pendingRegistration = email.lowercase() to session
        api.otpRequest(IapConfig.appBearer, OtpRequest(email, PURPOSE_VERIFY))
        AuthResult(message = "", user = User(id = session.id, name = session.name, email = session.email), role = IAP_SESSION_ROLE)
    }.mapError()

    override suspend fun resendVerificationCode(email: String): Result<Unit> = caller.result {
        api.otpRequest(IapConfig.appBearer, OtpRequest(email.trim(), PURPOSE_VERIFY))
        Unit
    }.mapError()

    override suspend fun verifyOtp(email: String, otp: String): Result<UserSession> = caller.result {
        val clean = email.trim()
        val answer = api.otpVerify(IapConfig.appBearer, OtpVerifyRequest(clean, PURPOSE_VERIFY, otp.trim()))
        // The verification itself issues no token: registration already did
        val session = pendingRegistration?.takeIf { it.first == clean.lowercase() }?.second
            ?: answer.token?.let { token -> AuthSessionDto(token, answer.member).toSession(fallbackName = "") }
            ?: throw PremiumException(PremiumProblem.SESSION_EXPIRED, serverMessage = "Please sign in with your email and password.")
        pendingRegistration = null
        signedIn(session)
    }.mapError()

    override suspend fun login(email: String, password: String): Result<UserSession> = caller.result {
        val answer = api.login(IapConfig.appBearer, LoginRequest(identifier = email.trim(), password = password))
        signedIn(answer.toSession(fallbackName = ""))
    }.mapError()

    override suspend fun signInWithGoogle(context: PlatformContext): Result<GoogleUser> =
        runCatching { googleSignIn.signIn(context) }.onFailure { Log.e(TAG, "Google sign-in failed", it) }

    override suspend fun loginWithGoogle(idToken: String): Result<UserSession> = caller.result {
        val answer = api.oauthLogin(IapConfig.appBearer, OAuthLoginRequest(provider = "google", idToken = idToken))
        signedIn(answer.toSession(fallbackName = ""))
    }.mapError()

    override val isAppleSignInAvailable: Boolean get() = appleSignIn != null

    override suspend fun signInWithApple(): Result<UserSession> {
        val provider = appleSignIn ?: return Result.failure(UnsupportedOperationException("Sign in with Apple is not available"))
        val apple = runCatching { provider.signIn() }.onFailure { Log.e(TAG, "Apple sign-in failed", it) }
            .getOrElse { return Result.failure(it) }
        return caller.result {
            val answer = api.oauthLogin(IapConfig.appBearer, OAuthLoginRequest(provider = "apple", idToken = apple.idToken))
            // Apple sends the name only on the first sign-in; the backend may not have one yet
            signedIn(answer.toSession(fallbackName = apple.name.orEmpty()))
        }.mapError()
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> = caller.result {
        api.otpRequest(IapConfig.appBearer, OtpRequest(email.trim(), PURPOSE_RESET))
        Unit
    }.mapError()

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<String> = caller.result {
        val answer = api.otpVerify(IapConfig.appBearer, OtpVerifyRequest(email.trim(), PURPOSE_RESET, code.trim()))
        answer.resetToken?.takeIf { it.isNotBlank() } ?: throw PremiumException(PremiumProblem.UNKNOWN)
    }.mapError()

    override suspend fun resetPassword(email: String, resetToken: String, newPassword: String): Result<Unit> = caller.result {
        api.resetPassword(IapConfig.appBearer, ResetPasswordRequest(email.trim(), resetToken, newPassword))
    }.mapError()

    override fun getSession(): Flow<UserSession?> = userPreferences.userSession

    override suspend fun saveSession(session: UserSession) {
        userPreferences.saveUserSession(session)
    }

    /**
     * Notifications stop going to this phone for the member first (while the token still works), then the
     * session ends on the backend and on the phone, then this phone registers again as the guest.
     */
    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        val token = userPreferences.userSession.firstOrNull()?.takeIf { it.role == IAP_SESSION_ROLE }?.token
        if (token != null) {
            runCatching { push.unregister() }
            try {
                api.logout("Bearer $token")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // The local login is gone either way; the token simply runs out on the backend
            }
        }
        userPreferences.clearUserSession()
        userPreferences.saveSubscriptionStatus(isPro = false, planId = null, purchaseToken = null)
        entitlementLocalDataSource?.clearEntitlements()
        background.launch { push.register() }
        Result.success(Unit)
    }

    // Accounts are checked by the backend on every call now: an unknown session simply stops working
    override suspend fun checkUserExists(email: String): Result<Boolean> = Result.success(true)

    override suspend fun dropLegacySession() {
        val session = userPreferences.userSession.firstOrNull() ?: return
        if (session.role != IAP_SESSION_ROLE) {
            Log.i(TAG, "Signing out a login from the old dashboard backend")
            userPreferences.clearUserSession()
            entitlementLocalDataSource?.clearEntitlements()
        }
    }

    private suspend fun signedIn(session: UserSession): UserSession {
        if (session.token.isBlank()) throw PremiumException(PremiumProblem.SIGN_IN_REJECTED)
        userPreferences.saveUserSession(session)
        // Notifications now go to this member on this phone
        background.launch { push.register() }
        return session
    }

    private fun AuthSessionDto.toSession(fallbackName: String): UserSession {
        val m: MemberDto? = member
        val email = m?.email.orEmpty()
        return UserSession(
            token = token.orEmpty(),
            id = m?.id ?: 0,
            name = m?.name?.takeIf { it.isNotBlank() }
                ?: fallbackName.takeIf { it.isNotBlank() }
                ?: m?.username?.takeIf { it.isNotBlank() }
                ?: email.substringBefore('@'),
            email = email,
            avatar = m?.avatarUrl,
            role = IAP_SESSION_ROLE
        )
    }

    /** The screens show `error.message`: the backend's own sentence when it sent one, otherwise a plain one. */
    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { error ->
        val premium = error as? PremiumException
        val text = premium?.serverMessage ?: when (premium?.problem) {
            PremiumProblem.OFFLINE -> "No internet connection. Check it and try again."
            PremiumProblem.SIGN_IN_REJECTED -> "Sign-in didn't work. Please try again."
            PremiumProblem.STORE_BUSY -> "Too many attempts. Please wait a moment and try again."
            else -> "Something went wrong. Please try again."
        }
        throw Exception(text, error)
    }

    private companion object {
        const val TAG = "IapAuth"
        const val PURPOSE_VERIFY = "email_verification"
        const val PURPOSE_RESET = "password_reset"
    }
}
