package com.urdufonts.app.domain.repo

import android.content.Context
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.GoogleUser
import com.urdufonts.app.domain.models.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun registerUser(params: RegisterParams): Result<AuthResult>
    suspend fun signInWithGoogle(context: Context): Result<GoogleUser>
    suspend fun loginWithGoogle(idToken: String): Result<UserSession>
    fun getSession(): Flow<UserSession?>
    suspend fun saveSession(session: UserSession)
    suspend fun logout(): Result<Unit>
    suspend fun checkUserExists(email: String): Result<Boolean>
    suspend fun verifyOtp(email: String, otp: String): Result<UserSession>
    suspend fun login(email: String, password: String): Result<UserSession>
}

