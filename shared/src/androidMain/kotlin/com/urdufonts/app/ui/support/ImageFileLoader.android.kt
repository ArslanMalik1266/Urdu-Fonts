package com.urdufonts.app.ui.support

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val QUARTER_TURN = 90f
private const val HALF_TURN = 180f

@Composable
actual fun rememberImageFile(path: String, reloadKey: Any, maxSide: Int): ImageBitmap? {
    // A cached image is shown in the very first frame, so a returning row or screen never flashes empty.
    val cached = remember(path, reloadKey, maxSide) {
        ImageMemoryCache.keyFor(path, maxSide)?.let(ImageMemoryCache::get)?.asImageBitmap()
    }
    val image by produceState(initialValue = cached, path, reloadKey, maxSide) {
        if (value != null) return@produceState
        value = withContext(Dispatchers.IO) {
            val key = ImageMemoryCache.keyFor(path, maxSide) ?: return@withContext null
            val bitmap = ImageMemoryCache.get(key) ?: decodeUpright(path, maxSide)?.also { ImageMemoryCache.put(key, it) }
            bitmap?.asImageBitmap()
        }
    }
    return image
}

/** Decodes in sRGB and turns the photo the way its EXIF note says, like the gallery and the C engine do. */
private fun decodeUpright(path: String, maxSide: Int): Bitmap? {
    val bitmap = decodeDownsampled(path, maxSide) ?: return null
    val matrix = orientationMatrix(readOrientation(path)) ?: return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/**
 * Decodes the file no larger than it will be shown.
 *
 * Full-size images ([maxSide] above [THUMBNAIL_DECODE_SIDE]) keep full colour and are only halved by the
 * decoder until the longest side is under twice [maxSide], as before.
 *
 * Thumbnails are made small on purpose, because lists hold many of them and they must all fit in
 * [ImageMemoryCache] (otherwise a long list reloads its images on every scroll): after the decoder's
 * halving they are scaled so the shorter side is at most [THUMBNAIL_SHORT_SIDE] (they are shown cropped to
 * a square, so the shorter side is what fills the tile), and photos without transparency use 2 bytes per
 * pixel instead of 4.
 */
private fun decodeDownsampled(path: String, maxSide: Int): Bitmap? = BitmapFactory.Options().run {
    val thumbnail = maxSide <= THUMBNAIL_DECODE_SIDE
    inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, this)
    var sampleSize = 1
    while (outWidth / (sampleSize * 2) >= maxSide || outHeight / (sampleSize * 2) >= maxSide) {
        sampleSize *= 2
    }
    inJustDecodeBounds = false
    inSampleSize = sampleSize
    // Wide-gamut photos (e.g. Display P3) are converted to sRGB, so picked colours match what the app converts.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
    // Ignored by the decoder for images with transparency, which stay in full colour.
    if (thumbnail) inPreferredConfig = Bitmap.Config.RGB_565
    val decoded = BitmapFactory.decodeFile(path, this) ?: return@run null
    if (!thumbnail) return@run decoded
    val shortSide = minOf(decoded.width, decoded.height)
    if (shortSide <= THUMBNAIL_SHORT_SIDE) return@run decoded
    val scale = THUMBNAIL_SHORT_SIDE.toFloat() / shortSide
    val scaled = Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1), (decoded.height * scale).toInt().coerceAtLeast(1), true)
    if (scaled !== decoded) decoded.recycle()
    scaled
}

/** A thumbnail's shorter side in px: enough for a two-column grid tile on a phone. */
private const val THUMBNAIL_SHORT_SIDE = 384

private fun readOrientation(path: String): Int = try {
    ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
} catch (e: IOException) {
    ExifInterface.ORIENTATION_NORMAL
}

/** The turn and mirror for each EXIF orientation; null when the photo is already upright. */
private fun orientationMatrix(orientation: Int): Matrix? = when (orientation) {
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { setScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { setRotate(HALF_TURN) }
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { setScale(1f, -1f) }
    ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { setRotate(QUARTER_TURN); postScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { setRotate(QUARTER_TURN) }
    ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply { setRotate(-QUARTER_TURN); postScale(-1f, 1f) }
    ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { setRotate(-QUARTER_TURN) }
    else -> null
}
