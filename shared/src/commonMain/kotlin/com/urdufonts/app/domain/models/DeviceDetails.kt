package com.urdufonts.app.domain.models

/** The phone the app is running on, sent with push registrations and support tickets. */
data class DeviceDetails(
    val appVersion: String,
    val brand: String,
    val model: String,
    val osVersion: String,
    val language: String,
    /** The phone's time zone by its IANA name, such as "Asia/Karachi". */
    val timeZone: String,
)
