package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import kotlinx.coroutines.flow.firstOrNull

class CheckUserStatusUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        val session = repository.getSession().firstOrNull()
        if (session != null) {
            repository.checkUserExists(session.email).onSuccess { exists ->
                if (!exists) {
                    repository.logout()
                }
            }
        }
    }
}
