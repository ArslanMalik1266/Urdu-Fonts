package com.webscare.urdufonts.di

import com.webscare.urdufonts.data.repository.BannerRepositoryImpl
import com.webscare.urdufonts.data.repository.CategoriesRepositoryImpl
import com.webscare.urdufonts.data.repository.FontDetailRepositoryImpl
import com.webscare.urdufonts.data.repository.FontRepositoryImpl
import com.webscare.urdufonts.data.repository.StylesRepositoryImpl
import com.webscare.urdufonts.domain.repo.BannerRepository
import com.webscare.urdufonts.domain.repo.CategoriesRepository
import com.webscare.urdufonts.domain.repo.FontDetailRepository
import com.webscare.urdufonts.domain.repo.FontRepository
import com.webscare.urdufonts.domain.repo.StylesRepository
import org.koin.dsl.module

val dataModule = module {
    single<FontRepository> { FontRepositoryImpl() }
    single <BannerRepository>{ BannerRepositoryImpl() }
    single<StylesRepository> { StylesRepositoryImpl() }
    single<CategoriesRepository> { CategoriesRepositoryImpl() }
    single<FontDetailRepository> { FontDetailRepositoryImpl() }
}