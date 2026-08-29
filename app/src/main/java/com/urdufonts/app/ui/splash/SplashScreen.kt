package com.urdufonts.app.ui.splash

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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.figmaDropShadow
import com.urdufonts.app.ui.util.softShadow
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
    shadowAlpha: Float = 1f,
    iconContent: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .figmaDropShadow(
                    color = Color.Black.copy(alpha = 0.04f * shadowAlpha),
                    offsetX = 0.dp,
                    offsetY = 3.dp,
                    blur = 10.dp,
                    shape = RoundedCornerShape(14.dp)
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
private fun AnimatedFeatureBadgeWrapper(
    index: Int,
    isTriggered: Boolean,
    content: @Composable (shadowAlpha: Float) -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }
    var showShadow by remember { mutableStateOf(false) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            delay(index * 90L) // 90ms slower liquid stagger delay for silky smooth cascading
            startAnim = true
            // Delay shadow fade-in until slide motion completes landing
            delay(350L)
            showShadow = true
        }
    }

    val animatedOffsetY by animateDpAsState(
        targetValue = if (startAnim) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "badge_offset_$index"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "badge_alpha_$index"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.84f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "badge_scale_$index"
    )

    val shadowAlpha by animateFloatAsState(
        targetValue = if (showShadow) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "shadow_alpha_$index"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = animatedOffsetY.toPx()
            alpha = animatedAlpha
            scaleX = animatedScale
            scaleY = animatedScale
        }
    ) {
        content(shadowAlpha)
    }
}

@Composable
fun SplashScreen(
    isOnboardingCompleted: Boolean = false,
    onNavigateNext: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val adManager: com.urdufonts.app.ads.AdManager = org.koin.compose.koinInject()
    var isBrandingVisible by remember { mutableStateOf(false) }
    var isBadgesVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Preload App Open Ad ONLY if user has completed onboarding and is going directly to Home Screen
        if (isOnboardingCompleted && activity != null) {
            android.util.Log.d("WebsCareAdsLog", "SplashScreen -> Returning user detected (Onboarding completed). Preloading App Open Ad...")
            adManager.preloadAppOpen(activity)
        }

        // Step 1 (t = 60ms): Trigger 3D Logo Elastic Spring Pop & Brand Title Fade
        delay(60L)
        isBrandingVisible = true

        // Step 2 (t = 560ms): Trigger Feature Badges Row after 500ms delay
        delay(500L)
        isBadgesVisible = true

        // Step 3 (t = 3000ms): Allow full 3.0s branding & motion enjoyment (60 + 500 + 2440 = 3000ms)
        delay(1600L)

        if (isOnboardingCompleted && activity != null) {
            android.util.Log.d("WebsCareAdsLog", "SplashScreen -> 3.0s branding completed. Showing App Open Ad before Home Screen...")
            adManager.showAppOpenAd(activity) {
                onNavigateNext()
            }
        } else {
            android.util.Log.d("WebsCareAdsLog", "SplashScreen -> First-time user detected. Navigating to Onboarding cleanly without App Open Ad.")
            onNavigateNext()
        }
    }

    // 3D Logo Card Elastic Spring Animations (Original Speed)
    val logoScale by animateFloatAsState(
        targetValue = if (isBrandingVisible) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (isBrandingVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )

    val logoOffsetY by animateDpAsState(
        targetValue = if (isBrandingVisible) 0.dp else (-20).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_offset"
    )

    // Brand Title & Subtitle Fade & Slide Animations (Original Speed)
    val titleAlpha by animateFloatAsState(
        targetValue = if (isBrandingVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )

    val titleOffsetY by animateDpAsState(
        targetValue = if (isBrandingVisible) 0.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "title_offset"
    )

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
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        translationY = logoOffsetY.toPx()
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
                    .figmaDropShadow(
                        color = Color.Black.copy(alpha = 0.06f),
                        offsetX = 0.dp,
                        offsetY = 4.dp,
                        blur = 16.dp,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White),
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

            Spacer(modifier = Modifier.height(18.dp))

            // Main Brand Title & Subtitle with Animated Entrance
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = titleOffsetY.toPx()
                    alpha = titleAlpha
                }
            ) {
                Text(
                    text = "UrduFonts.com",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = NunitoFontFamily,
                    color = Color(0xFF185C37)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Urdu Font Library",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = Color(0xFF729B8D)
                )
            }

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
                AnimatedFeatureBadgeWrapper(index = 0, isTriggered = isBadgesVisible) { shadowAlpha ->
                    FeatureBadgeItem(title = "Browse", shadowAlpha = shadowAlpha) {
                        Text(
                            text = "Aa",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = NunitoFontFamily,
                            color = Color(0xFF185C37)
                        )
                    }
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 2. Preview (preview_icon_splash Image)
                AnimatedFeatureBadgeWrapper(index = 1, isTriggered = isBadgesVisible) { shadowAlpha ->
                    FeatureBadgeItem(title = "Preview", shadowAlpha = shadowAlpha) {
                        Icon(
                            painter = painterResource(id = R.drawable.preview_icon_splash),
                            contentDescription = "Preview",
                            tint = Color(0xFF185C37),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 3. Download (Cloud Download Icon)
                AnimatedFeatureBadgeWrapper(index = 2, isTriggered = isBadgesVisible) { shadowAlpha ->
                    FeatureBadgeItem(title = "Download", shadowAlpha = shadowAlpha) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cloud_download),
                            contentDescription = "ic_cloud_download",
                            tint = Color(0xFF185C37),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Divider(
                    color = Color(0xFF185C37).copy(alpha = 0.1f),
                    modifier = Modifier
                        .height(58.dp)
                        .width(1.dp)
                )

                // 4. Favorites (Outline Heart Icon)
                AnimatedFeatureBadgeWrapper(index = 3, isTriggered = isBadgesVisible) { shadowAlpha ->
                    FeatureBadgeItem(title = "Favorites", shadowAlpha = shadowAlpha) {
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
}
