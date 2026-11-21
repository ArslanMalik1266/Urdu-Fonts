package com.urdufonts.app.platform

import android.util.Log
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpEngine(): HttpClientEngine = OkHttp.create()

actual object AppLog {
    actual fun d(tag: String, message: String) { Log.d(tag, message) }
    actual fun i(tag: String, message: String) { Log.i(tag, message) }
    actual fun w(tag: String, message: String, error: Throwable?) { Log.w(tag, message, error) }
    actual fun e(tag: String, message: String, error: Throwable?) { Log.e(tag, message, error) }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
