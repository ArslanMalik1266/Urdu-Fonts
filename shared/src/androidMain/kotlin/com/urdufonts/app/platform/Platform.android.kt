package com.urdufonts.app.platform

actual typealias PlatformContext = android.content.Context

actual typealias PlatformActivity = android.app.Activity

actual typealias PlatformFile = java.io.File

actual val PlatformFile.absolutePathString: String get() = absolutePath

actual val platformName: String = "Android"
