package com.urdufonts.app.platform

/**
 * Platform types the shared code passes around without knowing what they are.
 * Android: plain typealiases to Context / Activity / java.io.File, so existing Android code is unchanged.
 * iOS: lightweight stand-ins (see iosMain) to be backed by UIKit / Foundation types.
 */

/** Android `Context`; on iOS, whatever object a platform API needs to run (e.g. the app scene). */
expect abstract class PlatformContext

/** Android `Activity` (needed to launch Play Billing / Credential Manager UI); on iOS, the presenting screen. */
expect open class PlatformActivity

/** A file on the device: Android `java.io.File`; on iOS, a path in the app sandbox. */
expect open class PlatformFile

/** Absolute path of a [PlatformFile], usable from common code. */
expect val PlatformFile.absolutePathString: String

/** "Android" or "iOS", e.g. for analytics or the support ticket device details. */
expect val platformName: String
