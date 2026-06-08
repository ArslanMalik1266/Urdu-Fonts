package com.webscare.urdufonts.di

import com.webscare.urdufonts.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
}
