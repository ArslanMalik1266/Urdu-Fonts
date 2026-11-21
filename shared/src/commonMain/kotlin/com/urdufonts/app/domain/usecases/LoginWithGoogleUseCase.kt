package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.UserSession

class LoginWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<UserSession> {
        return repository.loginWithGoogle(idToken).onSuccess { session ->
            repository.saveSession(session)
        }
    }
}
