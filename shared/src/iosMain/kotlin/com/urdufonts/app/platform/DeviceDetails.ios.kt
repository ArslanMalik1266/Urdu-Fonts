package com.urdufonts.app.platform

import com.urdufonts.app.domain.models.DeviceDetails
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.localTimeZone
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

/** The iPhone's details, read fresh each time. */
class IosDeviceDetailsSource : DeviceDetailsSource {
    override fun current(): DeviceDetails {
        val device = UIDevice.currentDevice
        return DeviceDetails(
            appVersion = AppBuildConfig.versionName,
            brand = "Apple",
            model = hardwareModel() ?: device.model,
            osVersion = "${device.systemName} ${device.systemVersion}",
            language = NSLocale.currentLocale.localeIdentifier.replace('_', '-'),
            timeZone = NSTimeZone.localTimeZone.name
        )
    }

    /** e.g. "iPhone15,2" — more useful to support than the plain "iPhone". */
    @OptIn(ExperimentalForeignApi::class)
    private fun hardwareModel(): String? = memScoped {
        val info = alloc<utsname>()
        if (uname(info.ptr) != 0) null else info.machine.toKString().takeIf { it.isNotBlank() }
    }
}
