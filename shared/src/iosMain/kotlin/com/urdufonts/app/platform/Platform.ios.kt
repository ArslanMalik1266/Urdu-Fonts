package com.urdufonts.app.platform

/** iOS needs no Context; kept so common signatures compile. */
actual abstract class PlatformContext

/** Will wrap the presenting UIViewController for StoreKit / Sign in with Google. */
actual open class PlatformActivity

/** A file in the app sandbox, identified by its absolute path. */
actual open class PlatformFile(val path: String)

actual val PlatformFile.absolutePathString: String get() = path

actual val platformName: String = "iOS"
