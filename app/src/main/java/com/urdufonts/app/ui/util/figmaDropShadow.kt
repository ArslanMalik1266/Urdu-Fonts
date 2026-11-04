package com.urdufonts.app.ui.util

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// 1. Drop Shadow Modifier
fun Modifier.figmaDropShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    shape: RoundedCornerShape
): Modifier = this.drawWithCache {
    val cornerRadius = shape.topStart.toPx(size, this)
    val blurPx = blur.toPx().coerceAtMost(24.dp.toPx())
    val offX = offsetX.toPx()
    val offY = offsetY.toPx()
    val width = size.width
    val height = size.height

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blurPx > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blurPx,
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(offX, offY)
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                width,
                height,
                cornerRadius,
                cornerRadius,
                paint.asFrameworkPaint()
            )
            canvas.restore()
        }
    }
}

// 2. High-Performance Inner Shadow Modifier with Cached Path Masks
fun Modifier.figmaInnerShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    shape: RoundedCornerShape
): Modifier = this.drawWithCache {
    val cornerRadius = shape.topStart.toPx(size, this)
    val blurPx = blur.toPx().coerceAtMost(24.dp.toPx())
    val offX = offsetX.toPx()
    val offY = offsetY.toPx()

    // 1. Create outer boundary rect path (cached in drawWithCache)
    val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
    val innerPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = rect,
                topLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                topRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
        )
    }

    // 2. Create larger outer masking rect path (cached in drawWithCache)
    val strokeWidth = blurPx * 2 + maxOf(abs(offX), abs(offY))
    val outerRect = rect.inflate(strokeWidth)
    val outerPath = Path().apply {
        addRect(outerRect)
    }

    // 3. Subtract inner path from outer path to create the inverse mask (cached in drawWithCache)
    val maskPath = Path.combine(
        PathOperation.Difference,
        outerPath,
        innerPath
    )

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blurPx > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blurPx,
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    onDrawWithContent {
        drawContent() // Draw card content/background first

        // Clip drawing to card container & apply inner shadow offset
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.clipPath(innerPath)
            canvas.translate(offX, offY)
            canvas.drawPath(maskPath, paint)
            canvas.restore()
        }
    }
}

// 3. Baked card background: drop shadow + white fill + inner shadows + border, rendered ONCE into a
//    bitmap and shared by every card of the same size. The live versions above re-run their blur filters
//    on every frame for every card, which made scrolling the categories grid drop to ~20 fps.
private data class BakedCardKey(val width: Int, val height: Int, val cornerPx: Float, val density: Float)

private val bakedCardCache = object : LinkedHashMap<BakedCardKey, ImageBitmap>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<BakedCardKey, ImageBitmap>?) = size > 8
}

fun Modifier.bakedFigmaCardBackground(shape: RoundedCornerShape): Modifier = this.drawWithCache {
    val width = size.width.toInt()
    val height = size.height.toInt()
    val cornerPx = shape.topStart.toPx(size, this)
    // Room for the outer drop shadow (max blur 24dp + 4dp offset) around the card
    val margin = (28.dp.toPx()).toInt() + 2
    val bitmap = if (width <= 0 || height <= 0) null else synchronized(bakedCardCache) {
        bakedCardCache.getOrPut(BakedCardKey(width, height, cornerPx, density)) {
            renderFigmaCard(width, height, cornerPx, margin, this)
        }
    }
    onDrawBehind {
        if (bitmap != null) drawImage(bitmap, topLeft = Offset(-margin.toFloat(), -margin.toFloat()))
    }
}

private fun renderFigmaCard(
    width: Int,
    height: Int,
    cornerPx: Float,
    margin: Int,
    density: androidx.compose.ui.unit.Density
): ImageBitmap = with(density) {
    val maxBlur = 24.dp.toPx()
    val bitmap = Bitmap.createBitmap(width + margin * 2, height + margin * 2, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.translate(margin.toFloat(), margin.toFloat())
    val w = width.toFloat()
    val h = height.toFloat()

    fun blurPaint(color: Color, blur: Dp) = android.graphics.Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        val blurPx = blur.toPx().coerceAtMost(maxBlur)
        if (blurPx > 0f) maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }

    // A. Drop shadow: X=4, Y=4, Blur=14, Black 4%
    canvas.save()
    canvas.translate(4.dp.toPx(), 4.dp.toPx())
    canvas.drawRoundRect(0f, 0f, w, h, cornerPx, cornerPx, blurPaint(Color.Black.copy(alpha = 0.04f), 14.dp))
    canvas.restore()

    // B. White card base
    val fill = android.graphics.Paint().apply { isAntiAlias = true; color = android.graphics.Color.WHITE }
    canvas.drawRoundRect(0f, 0f, w, h, cornerPx, cornerPx, fill)

    // C + D. Inner shadows, clipped to the card: Black 4% (blur 19) and #D5FFCE 32% (blur 46)
    val cardPath = android.graphics.Path().apply {
        addRoundRect(0f, 0f, w, h, cornerPx, cornerPx, android.graphics.Path.Direction.CW)
    }
    fun innerShadow(color: Color, blur: Dp) {
        val off = 6.dp.toPx()
        val stroke = blur.toPx().coerceAtMost(maxBlur) * 2 + off
        val mask = android.graphics.Path().apply {
            addRect(-stroke, -stroke, w + stroke, h + stroke, android.graphics.Path.Direction.CW)
            op(cardPath, android.graphics.Path.Op.DIFFERENCE)
        }
        canvas.save()
        canvas.clipPath(cardPath)
        canvas.translate(off, off)
        canvas.drawPath(mask, blurPaint(color, blur))
        canvas.restore()
    }
    innerShadow(Color.Black.copy(alpha = 0.04f), 19.dp)
    innerShadow(Color(0xFFD5FFCE).copy(alpha = 0.32f), 46.dp)

    // E. 1dp white border, drawn inside the card edge
    val strokePx = 1.dp.toPx()
    val border = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = strokePx
        color = android.graphics.Color.WHITE
    }
    val half = strokePx / 2f
    canvas.drawRoundRect(half, half, w - half, h - half, cornerPx - half, cornerPx - half, border)

    bitmap.asImageBitmap()
}
