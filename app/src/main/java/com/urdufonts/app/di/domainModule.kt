package com.urdufonts.app.di

import com.urdufonts.app.domain.usecases.DownloadFontToDeviceUseCase
import com.urdufonts.app.domain.usecases.GetBannersUseCase
import com.urdufonts.app.domain.usecases.GetCategoriesUseCase
import com.urdufonts.app.domain.usecases.GetFontDetailUseCase
import com.urdufonts.app.domain.usecases.GetFontPreviewUseCase
import com.urdufonts.app.domain.usecases.GetFontWeightsUseCase
import com.urdufonts.app.domain.usecases.GetFontsUseCase
import com.urdufonts.app.domain.usecases.GetStylesUseCase
import com.urdufonts.app.domain.usecases.RegisterUserUseCase
import com.urdufonts.app.domain.usecases.GoogleSignInUseCase
import com.urdufonts.app.domain.usecases.LoginWithGoogleUseCase
import com.urdufonts.app.domain.usecases.GetUserSessionUseCase
import com.urdufonts.app.domain.usecases.LogoutUseCase
import com.urdufonts.app.domain.usecases.CheckUserStatusUseCase
import com.urdufonts.app.domain.usecases.VerifyOtpUseCase
import com.urdufonts.app.domain.usecases.LoginUseCase
import com.urdufonts.app.domain.usecases.GetSubscriptionOptionsUseCase
import com.urdufonts.app.domain.usecases.PurchaseSubscriptionUseCase
import com.urdufonts.app.domain.usecases.RestoreSubscriptionUseCase
import com.urdufonts.app.domain.usecases.CheckSubscriptionStatusUseCase
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
    factory { GetSubscriptionOptionsUseCase(get()) }
    factory { PurchaseSubscriptionUseCase(get()) }
    factory { RestoreSubscriptionUseCase(get()) }
    factory { CheckSubscriptionStatusUseCase(get()) }
}
