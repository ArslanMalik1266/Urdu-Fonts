package com.urdufonts.app.di

import com.google.gson.Gson
import com.urdufonts.app.data.remote.NetworkConstants
import com.urdufonts.app.data.remote.api.AuthApiService
import com.urdufonts.app.data.remote.api.FontApiService
import com.urdufonts.app.data.remote.interceptor.ApiKeyInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {

    single { Gson() }
    single {
        HttpLoggingInterceptor().apply {
            // BODY logs headers too (X-API-Key, Authorization) plus emails and purchase tokens: debug builds only
            level = if (com.urdufonts.app.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
    }

    single { ApiKeyInterceptor(apiKey = NetworkConstants.API_KEY) }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<ApiKeyInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single { get<Retrofit>().create(FontApiService::class.java) }
    single { get<Retrofit>().create(AuthApiService::class.java) }
    single { get<Retrofit>().create(com.urdufonts.app.data.remote.api.EntitlementApiService::class.java) }

}