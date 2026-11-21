package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
