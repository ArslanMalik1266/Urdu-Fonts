package com.webscare.urdufonts.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.detailScreen.DownloadState
import com.webscare.urdufonts.ui.util.addPressEffect

@Composable
fun AnimatedDownloadButton(
    downloadState: DownloadState,
    progress: Float,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWideButton = isExpanded

    // Width responds to scroll even while downloading
    val targetWidth = if (isWideButton) 280.dp else 56.dp
    val width by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 400),
        label = "fab_width"
    )

    // Offset coordinates to slide/animate the button position on scroll
    val offsetX by animateDpAsState(
        targetValue = if (isWideButton) 0.dp else 112.dp,
        animationSpec = tween(durationMillis = 400),
        label = "fab_offset"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150),
        label = "download_progress"
    )

    // Track background remains Grey during downloads for both shapes
    val baseColor = when (downloadState) {
        DownloadState.DOWNLOADING -> Color(0xFFEEEEEE)
        DownloadState.DOWNLOADED -> AppColor
        DownloadState.IDLE -> AppColor
    }

    val animatedColor by animateColorAsState(
        targetValue = baseColor,
        animationSpec = tween(durationMillis = 400),
        label = "fab_color"
    )

    // Show text check delayed during width transitions
    val showText = width > 220.dp
    val percentValue = (progress * 100).toInt()

    Box(
        modifier = modifier
            .offset(x = offsetX)
            .width(width)
            .height(56.dp)
            .let { baseModifier ->
                // Disable press animation during downloading state
                if (downloadState != DownloadState.DOWNLOADING) {
                    baseModifier.addPressEffect { onClick() }
                } else {
                    baseModifier
                }
            }
            .clip(RoundedCornerShape(28.dp))
            .background(animatedColor)
           ,
        contentAlignment = Alignment.Center
    ) {
        // 🟢 Liquid horizontal progress fill: works for both wide and circular shapes!
        if (downloadState == DownloadState.DOWNLOADING) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(AppColor)
            )
        }

        // Foreground content layout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (downloadState) {
                DownloadState.DOWNLOADING -> {
                    if (showText) {
                        Text(
                            text = "Downloading",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progress > 0.45f) Color.White else HeadingBlackColor,
                            fontFamily = NunitoFontFamily
                        )
                    }

                    // Sliding Number Odometer Counter (Floats up/down on change)
                    AnimatedContent(
                        targetState = percentValue,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "percentage_slider"
                    ) { targetPercent ->
                        val textColor = if (progress > 0.45f || !showText) Color.White else HeadingBlackColor
                        Text(
                            text = "$targetPercent%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }

                DownloadState.DOWNLOADED -> {
                    Image(
                        painter = painterResource(R.drawable.ic_tick),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                    if (showText) {
                        Text(
                            text = "Downloaded",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }

                DownloadState.IDLE -> {
                    Image(
                        painter = painterResource(R.drawable.ic_download),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                    if (showText) {
                        Text(
                            text = "Download",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }
            }
        }
    }
}
