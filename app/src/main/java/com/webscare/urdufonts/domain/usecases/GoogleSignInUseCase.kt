package com.webscare.urdufonts.domain.usecases

import android.content.Context
import com.webscare.urdufonts.domain.repo.AuthRepository
import com.webscare.urdufonts.domain.models.GoogleUser

class GoogleSignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: Context): Result<GoogleUser> {
        return repository.signInWithGoogle(context)
    }
}
