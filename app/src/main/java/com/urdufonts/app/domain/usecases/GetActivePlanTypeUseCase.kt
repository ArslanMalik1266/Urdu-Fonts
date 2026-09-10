package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.repo.EntitlementRepository
import kotlinx.coroutines.flow.Flow

class GetActivePlanTypeUseCase(
    private val entitlementRepository: EntitlementRepository
) {
    operator fun invoke(): Flow<SubscriptionType?> {
        return entitlementRepository.activePlanTypeFlow
    }
}
