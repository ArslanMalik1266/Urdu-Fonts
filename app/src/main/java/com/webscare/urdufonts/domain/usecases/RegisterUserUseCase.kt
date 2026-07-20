package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.AuthRepository
import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.AuthResult

class RegisterUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(params: RegisterParams): Result<AuthResult> {
        return repository.registerUser(params)
    }
}
