package com.urdufonts.app.data.local

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/** A stable, anonymous id for this Android device (a SHA-256 of ANDROID_ID). */
object AndroidDeviceId {
    fun get(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(androidId.toByteArray(Charsets.UTF_8))
        val sha256Hex = hashBytes.joinToString("") { "%02x".format(it) }
        return "anon_android_$sha256Hex"
    }
}
