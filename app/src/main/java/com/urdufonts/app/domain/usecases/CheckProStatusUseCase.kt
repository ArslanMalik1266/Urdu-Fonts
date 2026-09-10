package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.EntitlementRepository
import kotlinx.coroutines.flow.Flow

class CheckProStatusUseCase(
    private val entitlementRepository: EntitlementRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return entitlementRepository.isProUserFlow
    }
}
