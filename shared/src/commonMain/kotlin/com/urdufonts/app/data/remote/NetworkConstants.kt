package com.urdufonts.app.data.remote

import com.urdufonts.app.platform.AppBuildConfig

object NetworkConstants {
    const val BASE_URL = "https://dashboard.urdufonts.com/api/"

    const val IMAGE_BASE_URL = "https://dashboard.urdufonts.com"

    val API_KEY: String get() = AppBuildConfig.apiKey
}
