package com.urdufonts.app.di

import androidx.room.Room
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.data.local.db.AppDatabase
import com.urdufonts.app.data.repository.AuthRepositoryImpl
import com.urdufonts.app.data.repository.BannerRepositoryImpl
import com.urdufonts.app.data.repository.BillingRepositoryImpl
import com.urdufonts.app.data.repository.CategoriesRepositoryImpl
import com.urdufonts.app.data.repository.FontRepositoryImpl
import com.urdufonts.app.data.repository.StylesRepositoryImpl
import com.urdufonts.app.domain.repo.AuthRepository
import com.urdufonts.app.domain.repo.BannerRepository
import com.urdufonts.app.domain.repo.BillingRepository
import com.urdufonts.app.domain.repo.CategoriesRepository
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.domain.repo.StylesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import com.urdufonts.app.data.repository.MoreAppsRepositoryImpl
import com.urdufonts.app.data.repository.ProductsRepositoryImpl
import com.urdufonts.app.domain.repo.MoreAppsRepository
import com.urdufonts.app.domain.repo.ProductsRepository

val dataModule = module {

    single { UserPreferences(androidContext()) }

    // ── Room Database ──────────────────────────────────────────────────────
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "urdufonts.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    // ── DAOs ───────────────────────────────────────────────────────────────
    single { get<AppDatabase>().fontDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().styleDao() }

    single { com.urdufonts.app.data.local.EntitlementLocalDataSource(androidContext()) }
    single { com.urdufonts.app.data.remote.datasource.EntitlementRemoteDataSource(apiService = get(), identity = get()) }
    single<com.urdufonts.app.domain.repo.EntitlementRepository> {
        com.urdufonts.app.data.repository.EntitlementRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            userPreferences = get()
        )
    }

    single<FontRepository>       { FontRepositoryImpl(apiService = get(), fontDao = get(), context = get() ) }
    single<BannerRepository>     { BannerRepositoryImpl() }
    single<StylesRepository>     { StylesRepositoryImpl(apiService = get(), styleDao = get()) }
    single<CategoriesRepository> { CategoriesRepositoryImpl(apiService = get(), categoryDao = get()) }
    single<AuthRepository>       { AuthRepositoryImpl(api = get(), caller = get(), userPreferences = get(), push = get(), entitlementLocalDataSource = get()) }

    // ── IAP: guest identity, support tickets, push ─────────────────────────
    single { com.urdufonts.app.data.iap.IapPreferences(androidContext()) }
    single { com.urdufonts.app.data.iap.IapIdentityProvider(api = get(), caller = get(), userPreferences = get(), preferences = get()) }
    single<com.urdufonts.app.domain.repo.DeviceDetailsSource> { com.urdufonts.app.data.iap.AndroidDeviceDetailsSource() }
    single<com.urdufonts.app.domain.repo.PushTokenSource> { com.urdufonts.app.data.iap.push.FirebasePushTokenSource() }
    single<com.urdufonts.app.domain.repo.SupportRepository> {
        com.urdufonts.app.data.iap.SupportRepositoryImpl(context = androidContext(), api = get(), caller = get(), identity = get(), deviceDetails = get())
    }
    single<com.urdufonts.app.domain.repo.PushRepository> {
        com.urdufonts.app.data.iap.PushRepositoryImpl(api = get(), caller = get(), identity = get(), tokens = get(), preferences = get(), deviceDetails = get())
    }
    single { com.urdufonts.app.domain.models.NotificationTapInbox() }
    single<BillingRepository>    { BillingRepositoryImpl(context = androidContext(), userPreferences = get(), entitlementRepository = get(), iapApi = get(), identity = get()) }
    single<MoreAppsRepository>   { MoreAppsRepositoryImpl(apiService = get()) }
    single<ProductsRepository>   { ProductsRepositoryImpl(apiService = get()) }

}