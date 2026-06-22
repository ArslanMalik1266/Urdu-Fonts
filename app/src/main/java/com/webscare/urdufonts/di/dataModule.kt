package com.webscare.urdufonts.di

import androidx.room.Room
import com.webscare.urdufonts.data.local.db.AppDatabase
import com.webscare.urdufonts.data.repository.BannerRepositoryImpl
import com.webscare.urdufonts.data.repository.CategoriesRepositoryImpl
import com.webscare.urdufonts.data.repository.FontRepositoryImpl
import com.webscare.urdufonts.data.repository.StylesRepositoryImpl
import com.webscare.urdufonts.domain.repo.BannerRepository
import com.webscare.urdufonts.domain.repo.CategoriesRepository
import com.webscare.urdufonts.domain.repo.FontRepository
import com.webscare.urdufonts.domain.repo.StylesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

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
    single<FontRepository>       { FontRepositoryImpl(apiService = get(), fontDao = get()) }
    single<BannerRepository>     { BannerRepositoryImpl() }
    single<StylesRepository>     { StylesRepositoryImpl(apiService = get(), styleDao = get()) }
    single<CategoriesRepository> { CategoriesRepositoryImpl(apiService = get(), categoryDao = get()) }
}