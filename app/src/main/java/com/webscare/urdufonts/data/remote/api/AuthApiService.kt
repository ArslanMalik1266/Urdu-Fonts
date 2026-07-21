package com.webscare.urdufonts.data.remote.api

import com.webscare.urdufonts.data.remote.dto.RegisterResponseDto
import com.webscare.urdufonts.data.remote.dto.GoogleLoginRequestDto
import com.webscare.urdufonts.data.remote.dto.GoogleLoginResponseDto
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.FieldMap
import retrofit2.http.Body

interface AuthApiService {
    @POST("register/user")
    @FormUrlEncoded
    suspend fun registerUser(
        @FieldMap fields: Map<String, String>
    ): RegisterResponseDto

    @POST("google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequestDto
    ): GoogleLoginResponseDto
}
