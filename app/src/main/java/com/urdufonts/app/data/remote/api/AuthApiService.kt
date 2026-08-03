package com.urdufonts.app.data.remote.api

import com.urdufonts.app.data.remote.dto.RegisterResponseDto
import com.urdufonts.app.data.remote.dto.GoogleLoginRequestDto
import com.urdufonts.app.data.remote.dto.GoogleLoginResponseDto
import com.urdufonts.app.data.remote.dto.CheckUserResponseDto
import com.urdufonts.app.data.remote.dto.VerifyOtpResponseDto
import com.urdufonts.app.data.remote.dto.LoginResponseDto
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.Body
import retrofit2.http.Query

interface AuthApiService {
    @POST("register/user")
    suspend fun registerUser(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") pass: String
    ): RegisterResponseDto

    @POST("google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequestDto
    ): GoogleLoginResponseDto

    @POST("check_user")
    suspend fun checkUser(
        @Query("email") email: String
    ): CheckUserResponseDto

    @POST("register/verify-otp")
    suspend fun verifyOtp(
        @Query("email") email: String,
        @Query("otp") otp: String
    ): VerifyOtpResponseDto

    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): LoginResponseDto
}
