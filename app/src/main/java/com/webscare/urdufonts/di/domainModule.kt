package com.webscare.urdufonts.di

import com.webscare.urdufonts.domain.usecases.BuildFontListUseCase
import com.webscare.urdufonts.domain.usecases.GetBannersUseCase
import com.webscare.urdufonts.domain.usecases.GetCategoriesUseCase
import com.webscare.urdufonts.domain.usecases.GetFontDetailUseCase
import com.webscare.urdufonts.domain.usecases.GetFontPreviewUseCase
import com.webscare.urdufonts.domain.usecases.GetFontWeightsUseCase
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import com.webscare.urdufonts.domain.usecases.GetStylesUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetFontsUseCase(get()) }
    factory { BuildFontListUseCase() }
    factory { GetBannersUseCase(get()) }
    factory { GetStylesUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { GetFontDetailUseCase(get()) }
    factory { GetFontPreviewUseCase(get()) }
    factory { GetFontWeightsUseCase(get()) }
}
