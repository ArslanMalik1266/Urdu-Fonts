package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.AuthRepository
import com.webscare.urdufonts.domain.models.UserSession

class LoginWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<UserSession> {
        return repository.loginWithGoogle(idToken).onSuccess { session ->
            repository.saveSession(session)
        }
    }
}
