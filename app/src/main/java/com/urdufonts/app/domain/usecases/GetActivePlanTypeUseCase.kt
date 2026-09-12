package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.repo.BillingRepository
import kotlinx.coroutines.flow.Flow

class GetActivePlanTypeUseCase(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): Flow<SubscriptionType?> {
        return billingRepository.activePlanType
    }
}
