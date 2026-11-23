package com.urdufonts.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

/** Width of the app window in dp (replaces Android-only LocalConfiguration.screenWidthDp). */
@Composable
fun screenWidthDp(): Dp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }

/** Height of the app window in dp (replaces Android-only LocalConfiguration.screenHeightDp). */
@Composable
fun screenHeightDp(): Dp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
