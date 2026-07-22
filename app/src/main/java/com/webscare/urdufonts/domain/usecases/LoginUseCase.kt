package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.UserSession
import com.webscare.urdufonts.domain.repo.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        return repository.login(email, password)
    }
}
