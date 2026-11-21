package com.urdufonts.app.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.timeIntervalSince1970

actual fun createHttpEngine(): HttpClientEngine = Darwin.create()

actual object AppLog {
    actual fun d(tag: String, message: String) = log("D", tag, message, null)
    actual fun i(tag: String, message: String) = log("I", tag, message, null)
    actual fun w(tag: String, message: String, error: Throwable?) = log("W", tag, message, error)
    actual fun e(tag: String, message: String, error: Throwable?) = log("E", tag, message, error)

    private fun log(level: String, tag: String, message: String, error: Throwable?) {
        val text = if (error == null) message else "$message\n${error.stackTraceToString()}"
        // %@ keeps any % signs in the message from being read as format specifiers
        NSLog("%@", "$level/$tag: $text")
    }
}

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
