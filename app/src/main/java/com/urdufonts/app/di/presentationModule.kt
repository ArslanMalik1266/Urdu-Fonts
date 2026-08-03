package com.urdufonts.app.di

import com.urdufonts.app.ui.AppInitViewModel
import com.urdufonts.app.ui.category.CategoriesViewModel
import com.urdufonts.app.ui.detailScreen.FontDetailViewModel
import com.urdufonts.app.ui.fontList.FontListViewModel
import com.urdufonts.app.ui.home.HomeViewModel
import com.urdufonts.app.ui.onboarding.OnboardingViewModel
import com.urdufonts.app.ui.profile.ProfileViewModel
import com.urdufonts.app.ui.style.StylesViewModel
import com.urdufonts.app.ui.util.FontDownloadManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single { AppInitViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { StylesViewModel(get()) }
    viewModel { CategoriesViewModel(get()) }
    // Pass get() to resolve SavedStateHandle injection automatically
    viewModel { FontDetailViewModel(get(), get(), get(), get(),get()) }
    viewModel { FontListViewModel(get(), get()) } // Only needs GetFontsUseCase and SavedStateHandle
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    single { FontDownloadManager(get()) }
}
