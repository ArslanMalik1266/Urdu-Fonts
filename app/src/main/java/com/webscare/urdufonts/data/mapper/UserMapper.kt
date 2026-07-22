package com.webscare.urdufonts.data.mapper

import com.webscare.urdufonts.data.remote.dto.RegisterRequestDto
import com.webscare.urdufonts.data.remote.dto.RegisterResponseDto
import com.webscare.urdufonts.data.remote.dto.UserDto
import com.webscare.urdufonts.data.remote.dto.VerifyOtpResponseDto
import com.webscare.urdufonts.data.remote.dto.LoginResponseDto
import com.webscare.urdufonts.domain.models.AuthResult
import com.webscare.urdufonts.domain.models.RegisterParams
import com.webscare.urdufonts.domain.models.User

// Map Domain Inputs → Data DTO Request
fun RegisterParams.toDto(): RegisterRequestDto {
    return RegisterRequestDto(
        name = name,
        email = email,
        pass = pass
    )
}

// Map Data DTO Response → Domain Model User
fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email
    )
}

// Map Data DTO Response → Domain Model AuthResult
fun RegisterResponseDto.toDomain(): AuthResult {
    return AuthResult(
        message = message,
        user = User(id = 0, name = "", email = email),
        role = ""
    )
}

// Map Google Login response → Domain Model UserSession
fun com.webscare.urdufonts.data.remote.dto.GoogleLoginResponseDto.toDomain(): com.webscare.urdufonts.domain.models.UserSession {
    return com.webscare.urdufonts.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = user.avatar,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}

fun VerifyOtpResponseDto.toDomain(): com.webscare.urdufonts.domain.models.UserSession {
    return com.webscare.urdufonts.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = null,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}

fun LoginResponseDto.toDomain(): com.webscare.urdufonts.domain.models.UserSession {
    return com.webscare.urdufonts.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = null,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}
