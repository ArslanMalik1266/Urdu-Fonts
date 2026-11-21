package com.urdufonts.app.data.remote

import com.urdufonts.app.platform.AppBuildConfig
import com.urdufonts.app.platform.AppLog
import com.urdufonts.app.platform.createHttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * JSON settings that behave like the Gson setup this replaced: unknown fields are ignored, nulls are left out
 * of requests, and default values (e.g. `platform`) are always sent.
 */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    coerceInputValues = true
    isLenient = true
}

/** Pretty JSON for debug logs only. */
val PrettyJson: Json = Json(AppJson) { prettyPrint = true }

/**
 * Builds an [HttpClient] on this platform's engine. Non-2xx answers throw a Ktor `ResponseException`,
 * as Retrofit's `HttpException` did.
 */
fun createHttpClient(
    baseUrl: String,
    readTimeoutMillis: Long,
    extraHeaders: Map<String, () -> String> = emptyMap(),
): HttpClient = HttpClient(createHttpEngine()) {
    expectSuccess = true
    install(ContentNegotiation) { json(AppJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = readTimeoutMillis
    }
    // Bodies log headers too (X-API-Key, Authorization) plus emails and purchase tokens: debug builds only
    install(Logging) {
        level = if (AppBuildConfig.isDebug) LogLevel.BODY else LogLevel.NONE
        logger = object : Logger {
            override fun log(message: String) = AppLog.d("HttpClient", message)
        }
    }
    defaultRequest {
        url(baseUrl)
        extraHeaders.forEach { (name, value) -> headers.append(name, value()) }
    }
}
