package com.urdufonts.app.data.mapper

import com.urdufonts.app.data.remote.dto.RegisterRequestDto
import com.urdufonts.app.data.remote.dto.RegisterResponseDto
import com.urdufonts.app.data.remote.dto.UserDto
import com.urdufonts.app.data.remote.dto.VerifyOtpResponseDto
import com.urdufonts.app.data.remote.dto.LoginResponseDto
import com.urdufonts.app.domain.models.AuthResult
import com.urdufonts.app.domain.models.RegisterParams
import com.urdufonts.app.domain.models.User

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
fun com.urdufonts.app.data.remote.dto.GoogleLoginResponseDto.toDomain(): com.urdufonts.app.domain.models.UserSession {
    return com.urdufonts.app.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = user.avatar,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}

fun VerifyOtpResponseDto.toDomain(): com.urdufonts.app.domain.models.UserSession {
    return com.urdufonts.app.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = null,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}

fun LoginResponseDto.toDomain(): com.urdufonts.app.domain.models.UserSession {
    return com.urdufonts.app.domain.models.UserSession(
        token = token,
        id = user.id,
        name = user.name,
        email = user.email,
        avatar = null,
        role = user.roles?.firstOrNull() ?: "customer"
    )
}
