package com.urdufonts.app.ui.support

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Longest side decoded for full-width images; a 12 MP photo would otherwise need ~48 MB. */
const val FULL_DECODE_SIDE = 2048

/** Longest side decoded for list thumbnails, so a list of 20 images stays small in memory. */
const val THUMBNAIL_DECODE_SIDE = 512

/** A picture file decoded off the main thread, at most [maxSide] px on its short side, kept in a memory cache. */
@androidx.compose.runtime.Composable
expect fun rememberImageFile(path: String, reloadKey: Any, maxSide: Int): androidx.compose.ui.graphics.ImageBitmap?
