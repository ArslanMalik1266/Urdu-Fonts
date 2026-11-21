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

import com.urdufonts.app.domain.usecases.GetMoreAppsUseCase
import com.urdufonts.app.domain.usecases.GetProductsUseCase
import com.urdufonts.app.domain.usecases.SetPasswordUseCase

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
    factory { SetPasswordUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { LoginWithGoogleUseCase(get()) }
    factory { GetUserSessionUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckUserStatusUseCase(get(), get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { GetSubscriptionOptionsUseCase(get(), AndroidFeatureIcons) }
    factory { PurchaseSubscriptionUseCase(get()) }
    factory { RestoreSubscriptionUseCase(get()) }
    factory { CheckSubscriptionStatusUseCase(get()) }
    factory { GetMoreAppsUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.CheckProStatusUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.GetActivePlanTypeUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.GlobalSearchUseCase(get(), get(), get()) }

    // ── Help & Support ─────────────────────────────────────────────────────
    factory { com.urdufonts.app.domain.usecases.ObserveTicketsUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.RefreshTicketsUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.GetTicketUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.ValidateNewTicketUseCase() }
    factory { com.urdufonts.app.domain.usecases.ValidateAttachmentUseCase() }
    factory { com.urdufonts.app.domain.usecases.CreateTicketUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.ReplyToTicketUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.AnswerResolutionUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.RateTicketUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.GetAttachmentFileUseCase(get()) }

    // ── Push notifications ─────────────────────────────────────────────────
    factory { com.urdufonts.app.domain.usecases.RegisterPushTokenUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.RetryPushRegistrationUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.ObserveNotificationsEnabledUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.SetNotificationsEnabledUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.ObserveNotificationTapUseCase(get()) }
    factory { com.urdufonts.app.domain.usecases.CompleteNotificationTapUseCase(get(), get()) }
}

