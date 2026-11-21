package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.UserSession

class VerifyOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<UserSession> {
        return repository.verifyOtp(email, otp)
    }
}
