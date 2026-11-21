package com.urdufonts.app.domain.usecases

import com.urdufonts.app.platform.PlatformContext
import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.GoogleUser

class GoogleSignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: PlatformContext): Result<GoogleUser> {
        return repository.signInWithGoogle(context)
    }
}
