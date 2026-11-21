package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.BillingRepository

class RestoreSubscriptionUseCase(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return billingRepository.restorePurchases()
    }
}
