package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.UserSession
import com.urdufonts.app.domain.repo.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        return repository.login(email, password)
    }
}
