package com.urdufonts.app.data.remote.api

import com.urdufonts.app.data.remote.dto.CategoryResponseDto
import com.urdufonts.app.data.remote.dto.FontItemResponseDto
import com.urdufonts.app.data.remote.dto.StyleResponseDto
import retrofit2.http.GET
import retrofit2.http.Streaming
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Url

interface FontApiService {

    @GET("website/fonts")
    suspend fun getFonts(): FontItemResponseDto

    @GET("website/fonts/categories")
    suspend fun getCategories(): CategoryResponseDto

    @GET("website/fonts/styles")
    suspend fun getStyles(): StyleResponseDto

    @GET("more-apps")
    suspend fun getMoreApps(
        @retrofit2.http.Header("Authorization") authHeader: String = "Bearer 2|oto4ebwUe3Y3bqT6F2VRtopirO2F521yrMQ6Rtjsd01f1720"
    ): com.urdufonts.app.data.remote.dto.MoreAppsResponseDto

    @Streaming // Required to handle files without loading the whole thing into memory
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}