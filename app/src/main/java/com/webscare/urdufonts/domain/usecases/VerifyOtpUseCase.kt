package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.AuthRepository
import com.webscare.urdufonts.domain.models.UserSession

class VerifyOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<UserSession> {
        return repository.verifyOtp(email, otp)
    }
}
