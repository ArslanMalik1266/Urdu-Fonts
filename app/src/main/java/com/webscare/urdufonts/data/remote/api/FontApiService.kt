package com.webscare.urdufonts.data.remote.api

import com.webscare.urdufonts.data.remote.dto.CategoryResponseDto
import com.webscare.urdufonts.data.remote.dto.FontItemResponseDto
import com.webscare.urdufonts.data.remote.dto.StyleResponseDto
import retrofit2.http.GET

interface FontApiService {

    @GET("fonts")
    suspend fun getFonts(): FontItemResponseDto

    @GET("fonts/categories")
    suspend fun getCategories(): CategoryResponseDto

    @GET("fonts/styles")
    suspend fun getStyles(): StyleResponseDto
}