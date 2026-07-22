package com.webscare.urdufonts.di

import com.webscare.urdufonts.domain.usecases.DownloadFontToDeviceUseCase
import com.webscare.urdufonts.domain.usecases.GetBannersUseCase
import com.webscare.urdufonts.domain.usecases.GetCategoriesUseCase
import com.webscare.urdufonts.domain.usecases.GetFontDetailUseCase
import com.webscare.urdufonts.domain.usecases.GetFontPreviewUseCase
import com.webscare.urdufonts.domain.usecases.GetFontWeightsUseCase
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import com.webscare.urdufonts.domain.usecases.GetStylesUseCase
import com.webscare.urdufonts.domain.usecases.RegisterUserUseCase
import com.webscare.urdufonts.domain.usecases.GoogleSignInUseCase
import com.webscare.urdufonts.domain.usecases.LoginWithGoogleUseCase
import com.webscare.urdufonts.domain.usecases.GetUserSessionUseCase
import com.webscare.urdufonts.domain.usecases.LogoutUseCase
import com.webscare.urdufonts.domain.usecases.CheckUserStatusUseCase
import com.webscare.urdufonts.domain.usecases.VerifyOtpUseCase
import com.webscare.urdufonts.domain.usecases.LoginUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetFontsUseCase(get()) }
    factory { GetBannersUseCase(get()) }
    factory { GetStylesUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { GetFontDetailUseCase(get()) }
    factory { GetFontPreviewUseCase(get()) }
    factory { GetFontWeightsUseCase(get()) }
    factory { DownloadFontToDeviceUseCase(get()) }
    factory { RegisterUserUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { LoginWithGoogleUseCase(get()) }
    factory { GetUserSessionUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckUserStatusUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
}
