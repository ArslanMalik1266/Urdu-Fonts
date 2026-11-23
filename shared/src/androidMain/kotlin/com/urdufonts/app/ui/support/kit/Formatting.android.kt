package com.urdufonts.app.ui.support.kit

import android.text.format.DateFormat
import java.util.Date
import java.util.Locale

actual fun formatDay(epochMillis: Long): String {
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdyyyy")
    return DateFormat.format(pattern, Date(epochMillis)).toString()
}
