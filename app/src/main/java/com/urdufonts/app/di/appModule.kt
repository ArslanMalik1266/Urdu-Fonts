package com.urdufonts.app.di

val appModule = listOf(
    platformModule,     // shared: Android directories, Room database, device details
    networkModule,      // shared: Ktor clients and APIs
    sharedDataModule,   // shared: DataStore, DAOs, repositories
    adModule,
    dataModule,         // Android-only repositories
    domainModule,
    presentationModule
)
