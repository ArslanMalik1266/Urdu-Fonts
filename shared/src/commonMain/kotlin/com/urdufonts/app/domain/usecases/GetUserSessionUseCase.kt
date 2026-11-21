package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.UserSession
import kotlinx.coroutines.flow.Flow

class GetUserSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<UserSession?> = repository.getSession()
}
