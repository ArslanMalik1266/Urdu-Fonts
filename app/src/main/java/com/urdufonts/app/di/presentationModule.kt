package com.urdufonts.app.di

import com.urdufonts.app.ui.AppInitViewModel
import com.urdufonts.app.ui.category.CategoriesViewModel
import com.urdufonts.app.ui.detailScreen.FontDetailViewModel
import com.urdufonts.app.ui.fontList.FontListViewModel
import com.urdufonts.app.ui.home.HomeViewModel
import com.urdufonts.app.ui.onboarding.OnboardingViewModel
import com.urdufonts.app.ui.profile.ProfileViewModel
import com.urdufonts.app.ui.style.StylesViewModel
import com.urdufonts.app.ui.subscription.SubscriptionViewModel
import com.urdufonts.app.ui.util.FontDownloadManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single { AppInitViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), androidContext()) }
    viewModel { StylesViewModel(get(), androidContext()) }
    viewModel { CategoriesViewModel(get(), androidContext()) }
    // Pass get() to resolve SavedStateHandle injection automatically
    viewModel { FontDetailViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { FontListViewModel(get(), get(), androidContext()) } // Only needs GetFontsUseCase and SavedStateHandle
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { SubscriptionViewModel(get(), get(), get(), get()) }
    single { FontDownloadManager(get()) }
}
