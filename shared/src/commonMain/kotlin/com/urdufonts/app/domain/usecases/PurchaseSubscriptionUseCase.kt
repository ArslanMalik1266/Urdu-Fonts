package com.urdufonts.app.domain.usecases

import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.repo.BillingRepository

class PurchaseSubscriptionUseCase(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(activity: PlatformActivity, option: SubscriptionOption): Result<Unit> {
        return billingRepository.launchPurchase(activity, option)
    }
}
