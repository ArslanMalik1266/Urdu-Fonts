package com.webscare.urdufonts.di

import com.webscare.urdufonts.data.repository.BannerRepositoryImpl
import com.webscare.urdufonts.data.repository.FontRepositoryImpl
import com.webscare.urdufonts.domain.repo.BannerRepository
import com.webscare.urdufonts.domain.repo.FontRepository
import org.koin.dsl.module

val dataModule = module {
    single<FontRepository> { FontRepositoryImpl() }
    single <BannerRepository>{ BannerRepositoryImpl() }
}