package com.urdufonts.app.di

import com.urdufonts.app.data.iap.IapIdentityProvider
import com.urdufonts.app.data.iap.IapPreferences
import com.urdufonts.app.data.iap.PlanExpiryWatcher
import com.urdufonts.app.data.iap.PushRepositoryImpl
import com.urdufonts.app.data.iap.SupportRepositoryImpl
import com.urdufonts.app.data.local.AppDataStores
import com.urdufonts.app.data.local.EntitlementLocalDataSource
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.data.local.db.AppDatabase
import com.urdufonts.app.data.remote.datasource.EntitlementRemoteDataSource
import com.urdufonts.app.data.repository.CategoriesRepositoryImpl
import com.urdufonts.app.data.repository.EntitlementRepositoryImpl
import com.urdufonts.app.data.repository.MoreAppsRepositoryImpl
import com.urdufonts.app.data.repository.ProductsRepositoryImpl
import com.urdufonts.app.data.repository.StylesRepositoryImpl
import com.urdufonts.app.domain.models.NotificationTapInbox
import com.urdufonts.app.domain.repo.CategoriesRepository
import com.urdufonts.app.domain.repo.EntitlementRepository
import com.urdufonts.app.domain.repo.MoreAppsRepository
import com.urdufonts.app.domain.repo.ProductsRepository
import com.urdufonts.app.domain.repo.PushRepository
import com.urdufonts.app.domain.repo.StylesRepository
import com.urdufonts.app.domain.repo.SupportRepository
import org.koin.dsl.module

/**
 * Storage and repositories shared by Android and iOS. Needs [platformModule] (directories, database,
 * device details) and a [com.urdufonts.app.domain.repo.PushTokenSource] from the app.
 */
val sharedDataModule = module {

    // ── Preferences (DataStore) ────────────────────────────────────────────
    single { AppDataStores(get()) }
    single { UserPreferences(get<AppDataStores>().user) }
    single { EntitlementLocalDataSource(get<AppDataStores>().entitlement) }
    single { IapPreferences(get<AppDataStores>().iap) }

    // ── Room DAOs ──────────────────────────────────────────────────────────
    single { get<AppDatabase>().fontDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().styleDao() }

    // ── Entitlements ───────────────────────────────────────────────────────
    single { EntitlementRemoteDataSource(apiService = get(), identity = get()) }
    single<EntitlementRepository> {
        EntitlementRepositoryImpl(remoteDataSource = get(), localDataSource = get(), userPreferences = get())
    }
    single { PlanExpiryWatcher(entitlementRepository = get(), userPreferences = get()) }

    // ── Catalogue ──────────────────────────────────────────────────────────
    single<StylesRepository> { StylesRepositoryImpl(apiService = get(), styleDao = get()) }
    single<CategoriesRepository> { CategoriesRepositoryImpl(apiService = get(), categoryDao = get()) }
    single<MoreAppsRepository> { MoreAppsRepositoryImpl(apiService = get()) }
    single<ProductsRepository> { ProductsRepositoryImpl(apiService = get()) }

    // ── IAP: guest identity, support tickets, push ─────────────────────────
    single { IapIdentityProvider(api = get(), caller = get(), userPreferences = get(), preferences = get()) }
    single<SupportRepository> {
        SupportRepositoryImpl(directories = get(), api = get(), caller = get(), identity = get(), deviceDetails = get())
    }
    single<PushRepository> {
        PushRepositoryImpl(api = get(), caller = get(), identity = get(), tokens = get(), preferences = get(), deviceDetails = get())
    }
    single { NotificationTapInbox() }
    single<com.urdufonts.app.domain.repo.BannerRepository> { com.urdufonts.app.data.repository.BannerRepositoryImpl() }

    // ── Accounts (Google's account picker comes from the platform: GoogleSignInProvider) ──
    single<com.urdufonts.app.domain.repo.AuthRepository> {
        com.urdufonts.app.data.repository.AuthRepositoryImpl(api = get(), caller = get(), userPreferences = get(), push = get(), googleSignIn = get(), entitlementLocalDataSource = get())
    }
}
