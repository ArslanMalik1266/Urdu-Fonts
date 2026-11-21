package com.urdufonts.app.di

import com.urdufonts.app.data.iap.IapApi
import com.urdufonts.app.data.iap.IapCaller
import com.urdufonts.app.data.iap.IapConfig
import com.urdufonts.app.data.remote.NetworkConstants
import com.urdufonts.app.data.remote.api.EntitlementApiService
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.data.remote.createHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Network layer shared by Android and iOS. */
val networkModule = module {

    // urdufonts.com dashboard: every request carries X-API-Key
    single(named("dashboard")) {
        createHttpClient(
            baseUrl = NetworkConstants.BASE_URL,
            readTimeoutMillis = 30_000,
            extraHeaders = mapOf("X-API-Key" to { NetworkConstants.API_KEY })
        )
    }
    single { FontApiService(get(named("dashboard"))) }
    single { EntitlementApiService(get(named("dashboard"))) }

    // IAP backend (iap.webscare.app): sign-in, support tickets, push.
    // Its own client: the urdufonts.com X-API-Key must never be sent there. Long read timeout for uploads.
    single(named("iap")) { createHttpClient(baseUrl = IapConfig.BASE_URL, readTimeoutMillis = 60_000) }
    single { IapApi(get(named("iap"))) }
    single { IapCaller() }
}
