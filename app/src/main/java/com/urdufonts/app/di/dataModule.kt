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
import com.urdufonts.app.domain.repo.MoreAppsRepository

val dataModule = module {

    single { UserPreferences(androidContext()) }

    // ── Room Database ──────────────────────────────────────────────────────
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "urdufonts.db"
        ).build()
    }

    // ── DAOs ───────────────────────────────────────────────────────────────
    single { get<AppDatabase>().fontDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().styleDao() }

    // ── Repositories ───────────────────────────────────────────────────────
    single<FontRepository>       { FontRepositoryImpl(apiService = get(), fontDao = get(), context = get() ) }
    single<BannerRepository>     { BannerRepositoryImpl() }
    single<StylesRepository>     { StylesRepositoryImpl(apiService = get(), styleDao = get()) }
    single<CategoriesRepository> { CategoriesRepositoryImpl(apiService = get(), categoryDao = get()) }
    single<AuthRepository>       { AuthRepositoryImpl(apiService = get(), userPreferences = get()) }
    single<BillingRepository>    { BillingRepositoryImpl(context = androidContext(), userPreferences = get()) }
    single<MoreAppsRepository>   { MoreAppsRepositoryImpl(apiService = get()) }

}