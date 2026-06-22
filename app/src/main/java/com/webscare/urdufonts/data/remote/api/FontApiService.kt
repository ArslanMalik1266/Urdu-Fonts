package com.webscare.urdufonts.data.remote.api

import com.webscare.urdufonts.data.remote.dto.CategoryResponseDto
import com.webscare.urdufonts.data.remote.dto.FontItemResponseDto
import com.webscare.urdufonts.data.remote.dto.StyleResponseDto
import retrofit2.http.GET
import retrofit2.http.Streaming
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Url

interface FontApiService {

    @GET("fonts")
    suspend fun getFonts(): FontItemResponseDto

    @GET("fonts/categories")
    suspend fun getCategories(): CategoryResponseDto

    @GET("fonts/styles")
    suspend fun getStyles(): StyleResponseDto

    @Streaming // Required to handle files without loading the whole thing into memory
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}