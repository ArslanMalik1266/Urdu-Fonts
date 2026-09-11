package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.repo.AuthRepository

class SetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String? = null
    ): Result<AuthResult> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty."))
        }
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }
        if (confirmPassword != null && password != confirmPassword) {
            return Result.failure(IllegalArgumentException("Passwords do not match."))
        }

        val params = RegisterParams(
            name = name.trim(),
            email = email.trim(),
            pass = password
        )
        return authRepository.registerUser(params)
    }
}
