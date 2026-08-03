package com.urdufonts.app.di

import com.urdufonts.app.ads.AdManager
import com.urdufonts.app.ads.WebsCareAdManagerImpl
import org.koin.dsl.module

val adModule = module {
    single<AdManager> { WebsCareAdManagerImpl() }
}
