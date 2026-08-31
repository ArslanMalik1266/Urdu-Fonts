package com.urdufonts.app.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.onboarding.model.OnboardingPage
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import kotlin.math.abs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingItem(
    page: OnboardingPage,
    pageOffset: Float = 0f
) {
    val absOffset = abs(pageOffset).coerceIn(0f, 1f)
    val density = LocalDensity.current

    // Image depth scaling (scale down on exit, scale up on enter)
    val imageScale = 1f - (absOffset * 0.22f)
    val imageAlpha = 1f - (absOffset * 0.7f)
    val imageParallaxX = with(density) { (pageOffset * 80.dp.toPx()) }

    // Organic floating Y offsets for Screen 1's 5 dots
    val floatY1: Float
    val floatY2: Float
    val floatY3: Float
    val floatY4: Float
    val floatY5: Float

    if (page.dotsImageRes != null) {
        val dotsTransition = rememberInfiniteTransition(label = "dots_float_transition")
        val progress1 by dotsTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "dot1"
        )
        val progress2 by dotsTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(1900, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "dot2"
        )
        val progress3 by dotsTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2700, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "dot3"
        )
        val progress4 by dotsTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "dot4"
        )
        val progress5 by dotsTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "dot5"
        )

        floatY1 = with(density) { (-8.dp.toPx() + 14.dp.toPx() * progress1) }
        floatY2 = with(density) { (-6.dp.toPx() + 10.dp.toPx() * progress2) }
        floatY3 = with(density) { (-7.dp.toPx() + 12.dp.toPx() * progress3) }
        floatY4 = with(density) { (-5.dp.toPx() + 11.dp.toPx() * progress4) }
        floatY5 = with(density) { (-6.dp.toPx() + 12.dp.toPx() * progress5) }
    } else {
        floatY1 = 0f; floatY2 = 0f; floatY3 = 0f; floatY4 = 0f; floatY5 = 0f
    }

    // Entrance animation for Screen 3 (move up 75dp with same speed as scale up, pause, then scale up)
    val initialYOffsetPx = with(density) { 75.dp.toPx() }
    val entryYPx = remember { Animatable(initialYOffsetPx) }
    val entryScaleAnim = remember { Animatable(0.5f) }
    var hasAnimatedIn by remember { mutableStateOf(false) }

    if (page.hasFloatingAnimation) {
        val isCurrentPage = absOffset < 0.15f
        val isFullyOffscreen = absOffset >= 0.95f

        LaunchedEffect(isCurrentPage, isFullyOffscreen) {
            if (isCurrentPage && !hasAnimatedIn) {
                // Initial state: 75dp low and 0.5f scale
                entryYPx.snapTo(initialYOffsetPx)
                entryScaleAnim.snapTo(0.5f)

                val sharedEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

                // Step 1: Move UP from +75dp to 0dp with exact same speed & easing
                entryYPx.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 750, easing = sharedEasing)
                )

                // Short pause after aligning to position before scale-up
                delay(100)

                // Step 2: Scale UP from 0.5f to 1.0f with exact same speed & easing
                entryScaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 750, easing = sharedEasing)
                )

                hasAnimatedIn = true
            } else if (isFullyOffscreen) {
                // Reset ONLY when Screen 3 is completely off-screen (invisible)
                hasAnimatedIn = false
                entryYPx.snapTo(initialYOffsetPx)
                entryScaleAnim.snapTo(0.5f)
            }
        }
    }

    val currentEntryY = if (page.hasFloatingAnimation) entryYPx.value else 0f
    val currentEntryScale = if (page.hasFloatingAnimation) entryScaleAnim.value else 1f

    // Text vertical slide (exiting text slides down, entering text slides up from below)
    val textSlideY = with(density) { (absOffset * 100.dp.toPx()) }
    val textAlpha = 1f - (absOffset * 0.9f)
    val textScale = 1f - (absOffset * 0.12f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Central Main Image
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale * currentEntryScale
                        scaleY = imageScale * currentEntryScale
                        alpha = imageAlpha
                        translationX = imageParallaxX
                        translationY = currentEntryY
                    },
                contentScale = ContentScale.Fit
            )

            // Floating Dots around central image (Screen 1: 5 dots, Screen 2: 4 dots)
            if (page.dotsImageRes != null) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            alpha = imageAlpha
                            translationX = imageParallaxX
                        }
                ) {
                    val dotsPainter = painterResource(id = page.dotsImageRes)

                    if (page.dotCount == 4) {
                        // Screen 2: 4 Floating Dots pushed slightly outward
                        // Dot 1: Top-Left prominent 3D sphere (34dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(34.dp)
                                .align(Alignment.Center)
                                .offset(x = (-125).dp, y = (-85).dp)
                                .graphicsLayer { translationY = floatY1 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 2: Top-Right medium sphere (22dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                                .offset(x = 106.dp, y = (-135).dp)
                                .graphicsLayer { translationY = floatY2 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 3: Middle-Right small sphere (14dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.Center)
                                .offset(x = 112.dp, y = 24.dp)
                                .graphicsLayer { translationY = floatY3 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 4: Bottom-Left small sphere (15dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.Center)
                                .offset(x = (-128).dp, y = 112.dp)
                                .graphicsLayer { translationY = floatY4 },
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Screen 1: 5 Floating Dots pushed slightly outward
                        // Dot 1: Top-Left (24dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                                .offset(x = (-108).dp, y = (-78).dp)
                                .graphicsLayer { translationY = floatY1 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 2: Top-Center (13dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(13.dp)
                                .align(Alignment.Center)
                                .offset(x = (-5).dp, y = (-124).dp)
                                .graphicsLayer { translationY = floatY2 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 3: Top-Right (22dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                                .offset(x = 86.dp, y = (-96).dp)
                                .graphicsLayer { translationY = floatY3 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 4: Middle-Right (15dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.Center)
                                .offset(x = 116.dp, y = 10.dp)
                                .graphicsLayer { translationY = floatY4 },
                            contentScale = ContentScale.Fit
                        )

                        // Dot 5: Bottom-Left (16dp)
                        Image(
                            painter = dotsPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                                .offset(x = (-110).dp, y = 74.dp)
                                .graphicsLayer { translationY = floatY5 },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = textSlideY
                    alpha = textAlpha
                    scaleX = textScale
                    scaleY = textScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = page.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HeadingBlackColor,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.subtitle,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = GreyColor,
                lineHeight = 18.sp,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}