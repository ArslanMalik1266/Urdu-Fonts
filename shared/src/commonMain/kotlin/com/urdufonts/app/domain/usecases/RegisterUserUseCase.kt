package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.AuthResult

class RegisterUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(params: RegisterParams): Result<AuthResult> {
        return repository.registerUser(params)
    }
}
