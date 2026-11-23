package com.urdufonts.app.platform

import android.os.Build
import com.urdufonts.app.domain.models.DeviceDetails
import com.urdufonts.app.domain.repo.DeviceDetailsSource
import java.util.Locale
import java.util.TimeZone

/** The phone's details, read fresh each time. */
class AndroidDeviceDetailsSource : DeviceDetailsSource {
    override fun current(): DeviceDetails = DeviceDetails(
        appVersion = AppBuildConfig.versionName,
        brand = Build.MANUFACTURER.orEmpty(),
        model = Build.MODEL.orEmpty(),
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        language = Locale.getDefault().toLanguageTag(),
        timeZone = TimeZone.getDefault().id
    )
}
