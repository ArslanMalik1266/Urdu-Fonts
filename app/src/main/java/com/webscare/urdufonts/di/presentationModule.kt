package com.webscare.urdufonts.di

import com.webscare.urdufonts.ui.category.CategoriesViewModel
import com.webscare.urdufonts.ui.detailScreen.FontDetailViewModel
import com.webscare.urdufonts.ui.fontList.FontListViewModel
import com.webscare.urdufonts.ui.home.HomeViewModel
import com.webscare.urdufonts.ui.profile.ProfileViewModel
import com.webscare.urdufonts.ui.style.StylesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { StylesViewModel(get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { FontDetailViewModel(get()) }
    viewModel { FontListViewModel() }
    viewModel { ProfileViewModel() }
}
