package com.urdufonts.app.domain.repo

import android.app.Activity
import com.urdufonts.app.domain.models.SubscriptionOption
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val isProUser: Flow<Boolean>
    /** A purchase the backend did not accept, as a message for the user. */
    val purchaseProblems: Flow<String>
    fun startConnection()
    suspend fun getSubscriptionOptions(): List<SubscriptionOption>
    suspend fun launchPurchase(activity: Activity, option: SubscriptionOption): Result<Unit>
    suspend fun restorePurchases(): Result<Boolean>
    fun endConnection()
}
