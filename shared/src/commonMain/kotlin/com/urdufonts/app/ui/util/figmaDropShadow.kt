package com.urdufonts.app.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private fun blurPaint(color: Color, blurPx: Float): Paint = Paint().apply {
    isAntiAlias = true
    this.color = color
    if (blurPx > 0f) setBlur(blurPx)
}

private fun roundRectPath(left: Float, top: Float, right: Float, bottom: Float, corner: Float): Path = Path().apply {
    addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(corner)))
}

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
    val paint = blurPaint(color, blurPx)

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(offX, offY)
            canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, paint)
            canvas.restore()
        }
    }
}

// 2. Inner Shadow Modifier with cached path masks
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

    val rect = Rect(0f, 0f, size.width, size.height)
    val innerPath = roundRectPath(rect.left, rect.top, rect.right, rect.bottom, cornerRadius)
    val strokeWidth = blurPx * 2 + maxOf(abs(offX), abs(offY))
    val outerPath = Path().apply { addRect(rect.inflate(strokeWidth)) }
    // The area outside the card: blurred, it leaks back in as the inner shadow
    val maskPath = Path.combine(PathOperation.Difference, outerPath, innerPath)
    val paint = blurPaint(color, blurPx)

    onDrawWithContent {
        drawContent()
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

/** The 8 most recently used card backgrounds. Drawing happens on the UI thread, so no locking is needed. */
private val bakedCardCache = LinkedHashMap<BakedCardKey, ImageBitmap>()

fun Modifier.bakedFigmaCardBackground(shape: RoundedCornerShape): Modifier = this.drawWithCache {
    val width = size.width.toInt()
    val height = size.height.toInt()
    val cornerPx = shape.topStart.toPx(size, this)
    // Room for the outer drop shadow (max blur 24dp + 4dp offset) around the card
    val margin = (28.dp.toPx()).toInt() + 2
    val bitmap = if (width <= 0 || height <= 0) null else {
        val key = BakedCardKey(width, height, cornerPx, density)
        bakedCardCache.remove(key)?.also { bakedCardCache[key] = it } ?: renderFigmaCard(width, height, cornerPx, margin, this).also {
            bakedCardCache[key] = it
            while (bakedCardCache.size > 8) bakedCardCache.remove(bakedCardCache.keys.first())
        }
    }
    onDrawBehind {
        if (bitmap != null) drawImage(bitmap, topLeft = Offset(-margin.toFloat(), -margin.toFloat()))
    }
}

private fun renderFigmaCard(width: Int, height: Int, cornerPx: Float, margin: Int, density: Density): ImageBitmap = with(density) {
    val maxBlur = 24.dp.toPx()
    val image = ImageBitmap(width + margin * 2, height + margin * 2)
    val canvas = Canvas(image)
    canvas.translate(margin.toFloat(), margin.toFloat())
    val w = width.toFloat()
    val h = height.toFloat()
    fun shadowPaint(color: Color, blur: Dp) = blurPaint(color, blur.toPx().coerceAtMost(maxBlur))

    // A. Drop shadow: X=4, Y=4, Blur=14, Black 4%
    canvas.save()
    canvas.translate(4.dp.toPx(), 4.dp.toPx())
    canvas.drawRoundRect(0f, 0f, w, h, cornerPx, cornerPx, shadowPaint(Color.Black.copy(alpha = 0.04f), 14.dp))
    canvas.restore()

    // B. White card base
    canvas.drawRoundRect(0f, 0f, w, h, cornerPx, cornerPx, Paint().apply { isAntiAlias = true; color = Color.White })

    // C + D. Inner shadows, clipped to the card: Black 4% (blur 19) and #D5FFCE 32% (blur 46)
    val cardPath = roundRectPath(0f, 0f, w, h, cornerPx)
    fun innerShadow(color: Color, blur: Dp) {
        val off = 6.dp.toPx()
        val stroke = blur.toPx().coerceAtMost(maxBlur) * 2 + off
        val outer = Path().apply { addRect(Rect(-stroke, -stroke, w + stroke, h + stroke)) }
        val mask = Path.combine(PathOperation.Difference, outer, cardPath)
        canvas.save()
        canvas.clipPath(cardPath)
        canvas.translate(off, off)
        canvas.drawPath(mask, shadowPaint(color, blur))
        canvas.restore()
    }
    innerShadow(Color.Black.copy(alpha = 0.04f), 19.dp)
    innerShadow(Color(0xFFD5FFCE).copy(alpha = 0.32f), 46.dp)

    // E. 1dp white border, drawn inside the card edge
    val strokePx = 1.dp.toPx()
    val half = strokePx / 2f
    val border = Paint().apply {
        isAntiAlias = true
        style = PaintingStyle.Stroke
        strokeWidth = strokePx
        color = Color.White
    }
    canvas.drawRoundRect(half, half, w - half, h - half, cornerPx - half, cornerPx - half, border)

    image
}
