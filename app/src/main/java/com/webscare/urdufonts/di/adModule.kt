package com.webscare.urdufonts.di

import com.webscare.urdufonts.ads.AdManager
import com.webscare.urdufonts.ads.WebsCareAdManagerImpl
import org.koin.dsl.module

val adModule = module {
    single<AdManager> { WebsCareAdManagerImpl() }
}
