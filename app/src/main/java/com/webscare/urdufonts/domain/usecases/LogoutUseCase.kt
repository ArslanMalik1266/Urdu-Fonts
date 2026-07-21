package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
