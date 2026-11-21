package com.urdufonts.app.data.remote.api

import com.urdufonts.app.data.remote.dto.CategoryResponseDto
import com.urdufonts.app.data.remote.dto.FontItemResponseDto
import com.urdufonts.app.data.remote.dto.MoreAppsResponseDto
import com.urdufonts.app.data.remote.dto.StyleResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel

/** urdufonts.com dashboard API (fonts, categories, styles, more apps). */
class FontApiService(private val client: HttpClient) {

    suspend fun getFonts(): FontItemResponseDto = client.get("website/fonts").body()

    suspend fun getCategories(): CategoryResponseDto = client.get("website/fonts/categories").body()

    suspend fun getStyles(): StyleResponseDto = client.get("website/fonts/styles").body()

    suspend fun getMoreApps(
        authHeader: String = "Bearer 2|oto4ebwUe3Y3bqT6F2VRtopirO2F521yrMQ6Rtjsd01f1720"
    ): MoreAppsResponseDto = client.get("more-apps") { header("Authorization", authHeader) }.body()

    /**
     * Streams a file without loading it all into memory. [read] gets the body and its length (null when the
     * server does not say) and must finish reading before it returns.
     */
    suspend fun <T> downloadFile(url: String, read: suspend (body: ByteReadChannel, contentLength: Long?) -> T): T =
        client.prepareGet(url).execute { response -> read(response.bodyAsChannel(), response.contentLength()) }
}
