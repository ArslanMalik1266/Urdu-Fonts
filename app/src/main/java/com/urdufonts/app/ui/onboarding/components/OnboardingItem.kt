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

    // Text vertical slide (exiting text slides down, entering text slides up from below)
    val textSlideY = with(density) { (absOffset * 100.dp.toPx()) }
    val textAlpha = 1f - (absOffset * 0.9f)
    val textScale = 1f - (absOffset * 0.12f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                        alpha = imageAlpha
                        translationX = imageParallaxX
                    },
                contentScale = ContentScale.Fit
            )
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