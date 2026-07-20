package com.webscare.urdufonts.data.remote.api

import com.webscare.urdufonts.data.remote.dto.RegisterResponseDto
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.FieldMap

interface AuthApiService {
    @POST("register/user")
    @FormUrlEncoded
    suspend fun registerUser(
        @FieldMap fields: Map<String, String>
    ): RegisterResponseDto
}
