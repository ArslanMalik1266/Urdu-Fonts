package com.urdufonts.app.ui.support

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Longest side decoded for full-width images; a 12 MP photo would otherwise need ~48 MB. */
const val FULL_DECODE_SIDE = 2048

/** Longest side decoded for list thumbnails, so a list of 20 images stays small in memory. */
const val THUMBNAIL_DECODE_SIDE = 512

