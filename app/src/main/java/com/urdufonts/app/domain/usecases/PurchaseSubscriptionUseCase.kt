package com.urdufonts.app.domain.usecases

import android.app.Activity
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.repo.BillingRepository

class PurchaseSubscriptionUseCase(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(activity: Activity, option: SubscriptionOption): Result<Unit> {
        return billingRepository.launchPurchase(activity, option)
    }
}
