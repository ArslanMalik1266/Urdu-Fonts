package com.webscare.urdufonts.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.softShadow
import kotlinx.coroutines.delay

@Composable
private fun CloudDownloadIcon(
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Cloud Outline Path
        val cloudPath = Path().apply {
            moveTo(w * 0.22f, h * 0.62f)
            cubicTo(w * 0.08f, h * 0.62f, 0f, h * 0.46f, w * 0.12f, h * 0.34f)
            cubicTo(w * 0.12f, h * 0.14f, w * 0.38f, h * 0.08f, w * 0.5f, h * 0.24f)
            cubicTo(w * 0.65f, h * 0.08f, w * 0.88f, h * 0.18f, w * 0.85f, h * 0.34f)
            cubicTo(w * 0.98f, h * 0.46f, w * 0.92f, h * 0.62f, w * 0.78f, h * 0.62f)
        }
        drawPath(
            path = cloudPath,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Down Arrow
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.38f),
            end = Offset(w * 0.5f, h * 0.82f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.68f),
            end = Offset(w * 0.5f, h * 0.82f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.68f),
            end = Offset(w * 0.5f, h * 0.82f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun HeartIcon(
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(
                w * 0.08f, h * 0.55f,
                0f, h * 0.3f,
                w * 0.25f, h * 0.1f
            )
            cubicTo(
                w * 0.44f, 0f,
                w * 0.5f, h * 0.22f,
                w * 0.5f, h * 0.22f
            )
            cubicTo(
                w * 0.5f, h * 0.22f,
                w * 0.56f, 0f,
                w * 0.75f, h * 0.1f
            )
            cubicTo(
                w, h * 0.3f,
                w * 0.92f, h * 0.55f,
                w * 0.5f, h * 0.85f
            )
            close()
        }
        drawPath(
            path = heartPath,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun FeatureBadgeItem(
    title: String,
    iconContent: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .softShadow(
                    shadowColor = HeadingBlackColor.copy(alpha = 0.02f),
                    borderRadius = 14.dp,
                    blurValue = 16.dp,
                    offsetY = 0.dp
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = NunitoFontFamily,
            color = Color(0xFF334155)
        )
    }
}

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val adManager: com.webscare.urdufonts.ads.AdManager = org.koin.compose.koinInject()

    LaunchedEffect(Unit) {
        if (activity != null) {
            android.util.Log.d("WebsCareAdsLog", "SplashScreen LaunchedEffect -> Preloading App Open Ad silently in background...")
            adManager.preloadAppOpen(activity)
        }

        // Allow Splash Screen branding to display completely for 2.0 seconds
        delay(2000L)

        android.util.Log.d("WebsCareAdsLog", "SplashScreen -> Splash 2.0s completed. Navigating to HomeScreen first...")
        onNavigateNext()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.urdu_fonts_splash_screen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 1. PERFECT DEAD-CENTER BRANDING BLOCK (Logo Card + UrduFonts.com + Subtitle)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // White Rounded Card with Shadow containing Urdu Splash Icon
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                tonalElevation = 2.dp,
                modifier = Modifier.size(108.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .softShadow(
                            shadowColor = HeadingBlackColor.copy(alpha = 0.01f),
                            borderRadius = 26.dp,
                            blurValue = 26.dp,
                            offsetY = 0.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.urdu_icon_splash),
                        contentDescription = "Urdu Fonts Logo",
                        modifier = Modifier
                            .size(85.dp)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Brand Title
            Text(
                text = "UrduFonts.com",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                fontFamily = NunitoFontFamily,
                color = Color(0xFF185C37)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle
            Text(
                text = "Urdu Font Library",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily,
                color = Color(0xFF729B8D)
            )

            Spacer(modifier = Modifier.height(56.dp))

            // 2. LOWER 4 FEATURE BADGES ROW (Floating above bottom green curve arc)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 50.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Browse ("Aa")
                FeatureBadgeItem(title = "Browse") {
                    Text(
                        text = "Aa",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = NunitoFontFamily,
                        color = Color(0xFF185C37)
                    )
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 2. Preview (preview_icon_splash Image)
                FeatureBadgeItem(title = "Preview") {
                    Icon(
                        painter = painterResource(id = R.drawable.preview_icon_splash),
                        contentDescription = "Preview",
                        tint = Color(0xFF185C37),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 3. Download (Cloud Download Icon)
                FeatureBadgeItem(title = "Download") {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cloud_download),
                        contentDescription = "ic_cloud_download",
                        tint = Color(0xFF185C37),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 4. Favorites (Outline Heart Icon)
                FeatureBadgeItem(title = "Favorites") {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = "ic_favorite",
                        tint = Color(0xFF185C37),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
