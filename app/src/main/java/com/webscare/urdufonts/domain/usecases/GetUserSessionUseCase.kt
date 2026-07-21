package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.AuthRepository
import com.webscare.urdufonts.domain.models.UserSession
import kotlinx.coroutines.flow.Flow

class GetUserSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<UserSession?> = repository.getSession()
}
