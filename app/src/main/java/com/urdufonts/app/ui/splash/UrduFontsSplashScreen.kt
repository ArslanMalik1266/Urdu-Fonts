package com.urdufonts.app.ui.splash

/*
 * UrduFontsSplashScreen.kt
 * ------------------------
 * Pixel-proportional recreation of the UrduFonts.com splash reference,
 * drawn 100% on a single Compose Canvas (no images, no vector drawables).
 */

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Palette (sampled from the reference)
// ---------------------------------------------------------------------------
private val PaleBackdrop   = Color(0xFFE9EDE3) // sliver at top-right + behind waves
private val SurfaceWhite   = Color(0xFFF7F8F3) // main organic blob
private val IconTileWhite  = Color(0xFFFAFBF7) // neumorphic tiles
private val DeepGreen      = Color(0xFF15693F) // logo, title, icon strokes
private val SageSubtitle   = Color(0xFF93A896) // "Urdu Font Library"
private val LabelGrey      = Color(0xFF4A5450) // Browse / Preview / ...
private val DividerGrey    = Color(0xFFDDE3D9)
private val SageWaveTop    = Color(0xFFCFDFC9) // bottom sage gradient start
private val SageWaveBottom = Color(0xFFBBD2B3) // bottom sage gradient end
private val CreamWave      = Color(0xFFE6EBDF) // wave layer between white & sage
private val SoftShadow     = Color(0x33244A36) // ambient drop shadows
private val TileShadow     = Color(0x26325844)

// ---------------------------------------------------------------------------
// Public composable
// ---------------------------------------------------------------------------
@Composable
fun UrduFontsSplashScreen(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        drawBackdrop()
        drawBottomScenery()
        drawMainSurface()
        drawAppIcon()
        drawTitles(textMeasurer)
        drawFeatureRow(textMeasurer)
    }
}

// ---------------------------------------------------------------------------
// 1. Backdrop
// ---------------------------------------------------------------------------
private fun DrawScope.drawBackdrop() {
    drawRect(color = PaleBackdrop, size = size)
}

// ---------------------------------------------------------------------------
// 2. Bottom sage waves + pattern
// ---------------------------------------------------------------------------
private fun DrawScope.drawBottomScenery() {
    val w = size.width
    val h = size.height

    // --- Sage gradient body (from ~78% height down to the bottom edge) -----
    val sagePath = Path().apply {
        moveTo(0f, h * 0.86f)
        cubicTo(w * 0.25f, h * 0.815f, w * 0.60f, h * 0.795f, w, h * 0.78f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = sagePath,
        brush = Brush.verticalGradient(
            colors = listOf(SageWaveTop, SageWaveBottom),
            startY = h * 0.80f,
            endY = h
        )
    )

    // --- Subtle geometric pattern on the sage (faint arabesque dots) -------
    clipPath(sagePath) {
        val step = w * 0.055f
        val r = w * 0.010f
        val pattern = Color.White.copy(alpha = 0.10f)
        var row = 0
        var y = h * 0.80f
        while (y < h + step) {
            val xOffset = if (row % 2 == 0) 0f else step / 2f
            var x = xOffset
            while (x < w + step) {
                drawCircle(
                    color = pattern,
                    radius = r,
                    center = Offset(x, y),
                    style = Stroke(width = r * 0.6f)
                )
                x += step
            }
            y += step * 0.86f
            row++
        }
    }

    // --- Cream wave sitting on top of the sage, under the white surface ----
    val creamWave = Path().apply {
        moveTo(0f, h * 0.842f)
        cubicTo(w * 0.28f, h * 0.90f, w * 0.62f, h * 0.86f, w, h * 0.745f)
        lineTo(w, h * 0.86f)
        cubicTo(w * 0.60f, h * 0.875f, w * 0.25f, h * 0.895f, 0f, h * 0.93f)
        close()
    }
    drawPath(creamWave, color = CreamWave)

    // --- Soft white sweep overlapping the sage near the very bottom --------
    val bottomSweep = Path().apply {
        moveTo(0f, h * 0.955f)
        cubicTo(w * 0.35f, h * 0.905f, w * 0.72f, h * 0.94f, w, h * 1.02f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(bottomSweep, color = SurfaceWhite.copy(alpha = 0.55f))
}

// Small local clipPath helper (keeps imports tidy)
private inline fun DrawScope.clipPath(path: Path, block: DrawScope.() -> Unit) {
    drawIntoCanvas { it.save(); it.clipPath(path) }
    block()
    drawIntoCanvas { it.restore() }
}

// ---------------------------------------------------------------------------
// 3. Main organic white surface
// ---------------------------------------------------------------------------
private fun DrawScope.drawMainSurface() {
    val w = size.width
    val h = size.height

    val surface = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.64f, 0f)
        cubicTo(w * 0.845f, h * 0.035f, w * 0.975f, h * 0.145f, w, h * 0.30f)
        lineTo(w, h * 0.695f)
        cubicTo(w * 0.93f, h * 0.795f, w * 0.72f, h * 0.852f, w * 0.48f, h * 0.858f)
        cubicTo(w * 0.28f, h * 0.863f, w * 0.10f, h * 0.842f, 0f, h * 0.795f)
        close()
    }

    // Soft drop shadow under the curved edges
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = SurfaceWhite.toArgb()
            setShadowLayer(w * 0.045f, 0f, w * 0.012f, SoftShadow.toArgb())
        }
        canvas.nativeCanvas.drawPath(surface.asAndroidPath(), paint)
    }
    // Crisp fill on top of the shadow pass
    drawPath(surface, color = SurfaceWhite)
}

// ---------------------------------------------------------------------------
// 4. App icon + calligraphy
// ---------------------------------------------------------------------------
private fun DrawScope.drawAppIcon() {
    val w = size.width
    val h = size.height

    val iconSize = w * 0.252f
    val topLeft = Offset((w - iconSize) / 2f, h * 0.370f - iconSize / 2f)
    val corner = iconSize * 0.27f

    drawNeumorphicRoundRect(
        rect = Rect(topLeft, Size(iconSize, iconSize)),
        cornerRadius = corner,
        fill = IconTileWhite,
        shadowRadius = iconSize * 0.16f,
        shadowDy = iconSize * 0.05f
    )

    // Content rect for the calligraphy (with breathing room)
    val inset = iconSize * 0.14f
    drawLogoCalligraphy(
        Rect(
            topLeft + Offset(inset, inset),
            Size(iconSize - inset * 2, iconSize - inset * 2)
        )
    )
}

/**
 * Approximate hand-built strokes for "اُردُو".
 * Coordinates live in a 0..1 unit space inside [rect] (y grows downward).
 */
private fun DrawScope.drawLogoCalligraphy(rect: Rect) {
    fun p(x: Float, y: Float) = Offset(rect.left + x * rect.width, rect.top + y * rect.height)

    val mainStroke = Stroke(
        width = rect.width * 0.105f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    val thinStroke = Stroke(
        width = rect.width * 0.075f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    // --- pesh (ُ) above the alif, top-right --------------------------------
    val peshTop = Path().apply {
        moveTo(p(0.88f, 0.06f).x, p(0.88f, 0.06f).y)
        cubicTo(
            p(0.76f, 0.02f).x, p(0.76f, 0.02f).y,
            p(0.72f, 0.12f).x, p(0.72f, 0.12f).y,
            p(0.82f, 0.14f).x, p(0.82f, 0.14f).y
        )
        moveTo(p(0.80f, 0.15f).x, p(0.80f, 0.15f).y)
        lineTo(p(0.72f, 0.22f).x, p(0.72f, 0.22f).y)
    }
    drawPath(peshTop, DeepGreen, style = thinStroke)

    // --- big right stroke: ا + ر flowing together --------------------------
    val alifRa = Path().apply {
        moveTo(p(0.86f, 0.28f).x, p(0.86f, 0.28f).y)
        cubicTo(
            p(0.92f, 0.50f).x, p(0.92f, 0.50f).y,
            p(0.82f, 0.74f).x, p(0.82f, 0.74f).y,
            p(0.62f, 0.84f).x, p(0.62f, 0.84f).y
        )
        cubicTo(
            p(0.55f, 0.87f).x, p(0.55f, 0.87f).y,
            p(0.49f, 0.86f).x, p(0.49f, 0.86f).y,
            p(0.46f, 0.82f).x, p(0.46f, 0.82f).y
        )
    }
    drawPath(alifRa, DeepGreen, style = mainStroke)

    // --- middle stroke: د ---------------------------------------------------
    val dal = Path().apply {
        moveTo(p(0.58f, 0.34f).x, p(0.58f, 0.34f).y)
        cubicTo(
            p(0.66f, 0.46f).x, p(0.66f, 0.46f).y,
            p(0.60f, 0.62f).x, p(0.60f, 0.62f).y,
            p(0.44f, 0.66f).x, p(0.44f, 0.66f).y
        )
    }
    drawPath(dal, DeepGreen, style = thinStroke)

    // --- wao: filled head + descending tail --------------------------------
    drawCircle(
        color = DeepGreen,
        radius = rect.width * 0.085f,
        center = p(0.30f, 0.52f)
    )
    val waoTail = Path().apply {
        moveTo(p(0.33f, 0.57f).x, p(0.33f, 0.57f).y)
        cubicTo(
            p(0.34f, 0.72f).x, p(0.34f, 0.72f).y,
            p(0.22f, 0.86f).x, p(0.22f, 0.86f).y,
            p(0.06f, 0.86f).x, p(0.06f, 0.86f).y
        )
    }
    drawPath(waoTail, DeepGreen, style = mainStroke)

    // --- pesh (ُ) above the wao --------------------------------------------
    val peshWao = Path().apply {
        moveTo(p(0.40f, 0.28f).x, p(0.40f, 0.28f).y)
        cubicTo(
            p(0.28f, 0.24f).x, p(0.28f, 0.24f).y,
            p(0.24f, 0.34f).x, p(0.24f, 0.34f).y,
            p(0.34f, 0.36f).x, p(0.34f, 0.36f).y
        )
    }
    drawPath(peshWao, DeepGreen, style = thinStroke)
}

// ---------------------------------------------------------------------------
// 5. Title + subtitle
// ---------------------------------------------------------------------------
private fun DrawScope.drawTitles(textMeasurer: TextMeasurer) {
    val w = size.width
    val h = size.height

    // "UrduFonts.com"
    val titleStyle = TextStyle(
        color = DeepGreen,
        fontSize = (w * 0.094f).toSp(),
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = (-0.5).sp
    )
    val title = textMeasurer.measure(AnnotatedString("UrduFonts.com"), titleStyle)
    drawText(
        textLayoutResult = title,
        topLeft = Offset(
            (w - title.size.width) / 2f,
            h * 0.4815f - title.size.height / 2f
        )
    )

    // "Urdu Font Library"
    val subtitleStyle = TextStyle(
        color = SageSubtitle,
        fontSize = (w * 0.045f).toSp(),
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 0.4.sp
    )
    val subtitle = textMeasurer.measure(AnnotatedString("Urdu Font Library"), subtitleStyle)
    drawText(
        textLayoutResult = subtitle,
        topLeft = Offset(
            (w - subtitle.size.width) / 2f,
            h * 0.525f - subtitle.size.height / 2f
        )
    )
}

// ---------------------------------------------------------------------------
// 6. Feature row (tiles + labels + dividers)
// ---------------------------------------------------------------------------
private fun DrawScope.drawFeatureRow(textMeasurer: TextMeasurer) {
    val w = size.width
    val h = size.height

    val tileSize = w * 0.108f
    val tileCenterY = h * 0.638f
    val labelCenterY = h * 0.6825f
    val centersX = listOf(w * 0.20f, w * 0.40f, w * 0.60f, w * 0.80f)
    val labels = listOf("Browse", "Preview", "Download", "Favorites")

    // --- Tiles --------------------------------------------------------------
    centersX.forEachIndexed { i, cx ->
        val rect = Rect(
            Offset(cx - tileSize / 2f, tileCenterY - tileSize / 2f),
            Size(tileSize, tileSize)
        )
        drawNeumorphicRoundRect(
            rect = rect,
            cornerRadius = tileSize * 0.28f,
            fill = IconTileWhite,
            shadowRadius = tileSize * 0.18f,
            shadowDy = tileSize * 0.06f
        )

        val iconInset = tileSize * 0.22f
        val iconRect = Rect(
            rect.topLeft + Offset(iconInset, iconInset),
            Size(tileSize - iconInset * 2, tileSize - iconInset * 2)
        )
        when (i) {
            0 -> drawBrowseIcon(textMeasurer, rect)
            1 -> drawAinIcon(iconRect)
            2 -> drawDownloadIcon(iconRect)
            3 -> drawHeartIcon(iconRect)
        }
    }

    // --- Labels -------------------------------------------------------------
    val labelStyle = TextStyle(
        color = LabelGrey,
        fontSize = (w * 0.034f).toSp(),
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )
    labels.forEachIndexed { i, label ->
        val layout = textMeasurer.measure(AnnotatedString(label), labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                centersX[i] - layout.size.width / 2f,
                labelCenterY - layout.size.height / 2f
            )
        )
    }

    // --- Vertical dividers between the four groups --------------------------
    val dividerTop = tileCenterY - tileSize * 0.55f
    val dividerBottom = labelCenterY + tileSize * 0.28f
    listOf(w * 0.30f, w * 0.50f, w * 0.70f).forEach { x ->
        drawLine(
            color = DividerGrey,
            start = Offset(x, dividerTop),
            end = Offset(x, dividerBottom),
            strokeWidth = w * 0.0016f
        )
    }
}

// --- Individual feature icons ----------------------------------------------

/** "Aa" — rendered as text, centered in the tile. */
private fun DrawScope.drawBrowseIcon(textMeasurer: TextMeasurer, tileRect: Rect) {
    val style = TextStyle(
        color = DeepGreen,
        fontSize = (tileRect.width * 0.42f).toSp(),
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    )
    val layout = textMeasurer.measure(AnnotatedString("Aa"), style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            tileRect.center.x - layout.size.width / 2f,
            tileRect.center.y - layout.size.height / 2f
        )
    )
}

/** ع (ain) — small top curl + big open bowl, single continuous stroke. */
private fun DrawScope.drawAinIcon(rect: Rect) {
    fun p(x: Float, y: Float) = Offset(rect.left + x / 24f * rect.width, rect.top + y / 24f * rect.height)

    val path = Path().apply {
        // top head: opens to the right
        moveTo(p(16.5f, 5.5f).x, p(16.5f, 5.5f).y)
        cubicTo(p(10.5f, 3.8f).x, p(10.5f, 3.8f).y, p(8.2f, 7.2f).x, p(8.2f, 7.2f).y, p(12.5f, 9.0f).x, p(12.5f, 9.0f).y)
        // bottom bowl: sweeps down-left then back up to the right
        cubicTo(p(5.0f, 11.0f).x, p(5.0f, 11.0f).y, p(4.5f, 17.5f).x, p(4.5f, 17.5f).y, p(10.0f, 19.8f).x, p(10.0f, 19.8f).y)
        cubicTo(p(14.5f, 21.6f).x, p(14.5f, 21.6f).y, p(17.5f, 18.5f).x, p(17.5f, 18.5f).y, p(16.2f, 15.0f).x, p(16.2f, 15.0f).y)
    }
    drawPath(
        path, DeepGreen,
        style = Stroke(rect.width * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/** Cloud outline with a download arrow. */
private fun DrawScope.drawDownloadIcon(rect: Rect) {
    fun p(x: Float, y: Float) = Offset(rect.left + x / 24f * rect.width, rect.top + y / 24f * rect.height)
    val stroke = Stroke(rect.width * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Cloud outline, open at the bottom center for the arrow
    val cloud = Path().apply {
        moveTo(p(9.5f, 16.5f).x, p(9.5f, 16.5f).y)
        lineTo(p(6.5f, 16.5f).x, p(6.5f, 16.5f).y)
        cubicTo(p(3.2f, 16.5f).x, p(3.2f, 16.5f).y, p(2.6f, 12.6f).x, p(2.6f, 12.6f).y, p(5.4f, 11.8f).x, p(5.4f, 11.8f).y)
        cubicTo(p(4.6f, 8.6f).x, p(4.6f, 8.6f).y, p(7.6f, 6.4f).x, p(7.6f, 6.4f).y, p(9.9f, 7.8f).x, p(9.9f, 7.8f).y)
        cubicTo(p(11.0f, 5.0f).x, p(11.0f, 5.0f).y, p(15.4f, 5.2f).x, p(15.4f, 5.2f).y, p(16.1f, 8.2f).x, p(16.1f, 8.2f).y)
        cubicTo(p(19.2f, 8.0f).x, p(19.2f, 8.0f).y, p(21.2f, 11.2f).x, p(21.2f, 11.2f).y, p(19.4f, 13.6f).x, p(19.4f, 13.6f).y)
        cubicTo(p(21.0f, 15.2f).x, p(21.0f, 15.2f).y, p(19.6f, 16.5f).x, p(19.6f, 16.5f).y, p(17.5f, 16.5f).x, p(17.5f, 16.5f).y)
        lineTo(p(14.5f, 16.5f).x, p(14.5f, 16.5f).y)
    }
    drawPath(cloud, DeepGreen, style = stroke)

    // Arrow: shaft + chevron, poking slightly below the cloud
    val arrow = Path().apply {
        moveTo(p(12f, 11f).x, p(12f, 11f).y)
        lineTo(p(12f, 18.6f).x, p(12f, 18.6f).y)
        moveTo(p(9.4f, 16.2f).x, p(9.4f, 16.2f).y)
        lineTo(p(12f, 18.8f).x, p(12f, 18.8f).y)
        lineTo(p(14.6f, 16.2f).x, p(14.6f, 16.2f).y)
    }
    drawPath(arrow, DeepGreen, style = stroke)
}

/** Heart outline. */
private fun DrawScope.drawHeartIcon(rect: Rect) {
    fun p(x: Float, y: Float) = Offset(rect.left + x / 24f * rect.width, rect.top + y / 24f * rect.height)

    val path = Path().apply {
        moveTo(p(12f, 19.5f).x, p(12f, 19.5f).y)
        cubicTo(p(5.6f, 14.6f).x, p(5.6f, 14.6f).y, p(3.4f, 10.8f).x, p(3.4f, 10.8f).y, p(5.1f, 7.9f).x, p(5.1f, 7.9f).y)
        cubicTo(p(6.8f, 5.0f).x, p(6.8f, 5.0f).y, p(10.6f, 5.5f).x, p(10.6f, 5.5f).y, p(12f, 8.4f).x, p(12f, 8.4f).y)
        cubicTo(p(13.4f, 5.5f).x, p(13.4f, 5.5f).y, p(17.2f, 5.0f).x, p(17.2f, 5.0f).y, p(18.9f, 7.9f).x, p(18.9f, 7.9f).y)
        cubicTo(p(20.6f, 10.8f).x, p(20.6f, 10.8f).y, p(18.4f, 14.6f).x, p(18.4f, 14.6f).y, p(12f, 19.5f).x, p(12f, 19.5f).y)
        close()
    }
    drawPath(
        path, DeepGreen,
        style = Stroke(rect.width * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// ---------------------------------------------------------------------------
// Shared neumorphic helper
// ---------------------------------------------------------------------------
private fun DrawScope.drawNeumorphicRoundRect(
    rect: Rect,
    cornerRadius: Float,
    fill: Color,
    shadowRadius: Float,
    shadowDy: Float
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = fill.toArgb()
            setShadowLayer(shadowRadius, 0f, shadowDy, TileShadow.toArgb())
        }
        canvas.nativeCanvas.drawRoundRect(
            rect.left, rect.top, rect.right, rect.bottom,
            cornerRadius, cornerRadius, paint
        )
    }
    drawRoundRect(
        color = fill,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.65f),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = rect.width * 0.012f)
    )
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
private fun UrduFontsSplashPreview() {
    UrduFontsSplashScreen()
}
