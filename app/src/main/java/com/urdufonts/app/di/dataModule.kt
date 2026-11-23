package com.urdufonts.app.di

import com.urdufonts.app.data.repository.AuthRepositoryImpl
import com.urdufonts.app.data.repository.BillingRepositoryImpl
import com.urdufonts.app.data.repository.FontRepositoryImpl
import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.repo.BillingRepository
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.domain.repo.PushTokenSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * The Android-only part of the data layer: Google sign-in, Play Billing, FCM and saving fonts to Downloads.
 * Everything else comes from the shared module's [sharedDataModule] and [platformModule].
 */
val dataModule = module {
    single<com.urdufonts.app.domain.repo.BannerRepository> { com.urdufonts.app.data.repository.BannerRepositoryImpl() }
    single<PushTokenSource> { com.urdufonts.app.data.iap.push.FirebasePushTokenSource() }
    single<FontRepository> { FontRepositoryImpl(apiService = get(), fontDao = get(), context = get()) }
    single<AuthRepository> {
        AuthRepositoryImpl(api = get(), caller = get(), userPreferences = get(), push = get(), entitlementLocalDataSource = get())
    }
    single<BillingRepository> {
        BillingRepositoryImpl(context = androidContext(), userPreferences = get(), entitlementRepository = get(), iapApi = get(), identity = get())
    }
}
