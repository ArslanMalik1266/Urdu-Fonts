package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.AuthResult

interface AuthRepository {
    suspend fun registerUser(params: RegisterParams): Result<AuthResult>
}
