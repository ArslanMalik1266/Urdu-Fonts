package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.data.remote.api.AuthApiService
import com.webscare.urdufonts.data.mapper.toDto
import com.webscare.urdufonts.data.mapper.toDomain
import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.AuthResult
import com.webscare.urdufonts.domain.repo.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun registerUser(
        params: RegisterParams
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        runCatching {
            val requestMap = params.toDto().toFieldMap()
            apiService.registerUser(requestMap).toDomain()
        }
    }
}
