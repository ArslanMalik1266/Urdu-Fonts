package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.BillingRepository
import kotlinx.coroutines.flow.Flow

class CheckSubscriptionStatusUseCase(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return billingRepository.isProUser
    }
}
