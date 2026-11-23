package com.urdufonts.app.di

import org.koin.core.module.Module

/**
 * What each platform provides itself: [com.urdufonts.app.platform.AppDirectories], the Room
 * [com.urdufonts.app.data.local.db.AppDatabase] and the [com.urdufonts.app.domain.repo.DeviceDetailsSource].
 */
expect val platformModule: Module
