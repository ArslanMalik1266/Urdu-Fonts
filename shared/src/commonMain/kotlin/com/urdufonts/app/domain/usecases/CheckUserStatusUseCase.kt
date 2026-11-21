package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.repo.PushRepository

/**
 * At every app start: a login left over from the old urdufonts.com backend is signed out once (accounts now
 * live on the IAP backend), then this phone tells the backend where to send notifications.
 */
class CheckUserStatusUseCase(
    private val repository: AuthRepository,
    private val push: PushRepository,
) {
    suspend operator fun invoke() {
        repository.dropLegacySession()
        push.register()
    }
}
