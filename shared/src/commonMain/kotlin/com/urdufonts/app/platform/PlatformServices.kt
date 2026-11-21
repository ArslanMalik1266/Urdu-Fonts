package com.urdufonts.app.platform

import io.ktor.client.engine.HttpClientEngine

/** The HTTP engine for this platform: OkHttp on Android, NSURLSession (Darwin) on iOS. */
expect fun createHttpEngine(): HttpClientEngine

/** Logging that goes to Logcat on Android and to the Xcode console on iOS. */
expect object AppLog {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, error: Throwable? = null)
    fun e(tag: String, message: String, error: Throwable? = null)
}

/** Wall-clock time in milliseconds since the epoch. */
expect fun currentTimeMillis(): Long
