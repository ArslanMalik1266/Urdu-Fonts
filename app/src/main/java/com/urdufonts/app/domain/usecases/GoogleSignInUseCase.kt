package com.urdufonts.app.domain.usecases

import android.content.Context
import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.models.GoogleUser

class GoogleSignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: Context): Result<GoogleUser> {
        return repository.signInWithGoogle(context)
    }
}
