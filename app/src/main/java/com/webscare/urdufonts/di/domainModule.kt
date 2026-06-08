package com.webscare.urdufonts.di

import com.webscare.urdufonts.domain.usecases.BuildFontListUseCase
import com.webscare.urdufonts.domain.usecases.GetBannersUseCase
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetFontsUseCase(get()) }
    factory { BuildFontListUseCase() }
    factory { GetBannersUseCase(get()) }
}
