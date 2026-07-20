package com.webscare.urdufonts.di

import com.webscare.urdufonts.ui.AppInitViewModel
import com.webscare.urdufonts.ui.category.CategoriesViewModel
import com.webscare.urdufonts.ui.detailScreen.FontDetailViewModel
import com.webscare.urdufonts.ui.fontList.FontListViewModel
import com.webscare.urdufonts.ui.home.HomeViewModel
import com.webscare.urdufonts.ui.onboarding.OnboardingViewModel
import com.webscare.urdufonts.ui.profile.ProfileViewModel
import com.webscare.urdufonts.ui.style.StylesViewModel
import com.webscare.urdufonts.ui.util.FontDownloadManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single { AppInitViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { StylesViewModel(get()) }
    viewModel { CategoriesViewModel(get()) }
    // Pass get() to resolve SavedStateHandle injection automatically
    viewModel { FontDetailViewModel(get(), get(), get(), get(),get()) }
    viewModel { FontListViewModel(get(), get()) } // Only needs GetFontsUseCase and SavedStateHandle
    viewModel { ProfileViewModel() }
    viewModel { OnboardingViewModel(get()) }
    single { FontDownloadManager(get()) }
}
