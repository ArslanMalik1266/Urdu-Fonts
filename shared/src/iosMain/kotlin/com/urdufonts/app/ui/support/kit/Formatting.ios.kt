package com.urdufonts.app.ui.support.kit

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatDay(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        // Same template as Android's getBestDateTimePattern("MMMdyyyy"): "Dec 12, 2026" in the user's style
        setLocalizedDateFormatFromTemplate("MMMdyyyy")
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
