package com.urdufonts.app.platform

/**
 * Build-time values (keys and version) that the shared code needs. Each app fills them in once at start-up:
 * Android from BuildConfig in MyApplication, iOS from its Info.plist / xcconfig before Koin starts.
 * Secrets stay out of source on both platforms.
 */
object AppBuildConfig {
    /** urdufonts.com API key, sent as X-API-Key. */
    var apiKey: String = ""

    /** IAP backend (iap.webscare.app) app token. */
    var iapAppToken: String = ""

    var versionName: String = ""

    var isDebug: Boolean = false

    fun init(apiKey: String, iapAppToken: String, versionName: String, isDebug: Boolean) {
        this.apiKey = apiKey
        this.iapAppToken = iapAppToken
        this.versionName = versionName
        this.isDebug = isDebug
    }
}
