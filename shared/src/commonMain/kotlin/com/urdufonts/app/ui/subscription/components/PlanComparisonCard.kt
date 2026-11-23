package com.urdufonts.app.ui.subscription.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

/** Swallows what the inner list could not scroll, so nothing reaches the bottom sheet. */
private object ConsumeRemainingScroll : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        available.copy(x = 0f)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
        available.copy(x = 0f)
}

/**
 * The features of the selected plan, each with a ✓.
 * Always as tall as the plan with the fewest features (Basic), so the sheet never changes size;
 * the remaining rows scroll inside the card.
 */
@Composable
fun PlanComparisonCard(
    options: List<SubscriptionOption>,
    selected: SubscriptionOption,
    modifier: Modifier = Modifier
) {
    val sizingPlan = remember(options) { options.filter { it.features.isNotEmpty() }.minByOrNull { it.features.size } }
    val scrollState = rememberScrollState()

    LaunchedEffect(selected.id) { scrollState.animateScrollTo(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerLarge))
            .background(Color.White)
            .padding(Dimens.space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        AnimatedContent(
            targetState = selected.title,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "comparisonTitle"
        ) { title ->
            Text(
                text = "What you get with $title",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = HeadingBlackColor
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            // Invisible: gives the box exactly the height of the shortest plan's list
            Column(
                modifier = Modifier.alpha(0f),
                verticalArrangement = Arrangement.spacedBy(Dimens.space8)
            ) {
                sizingPlan?.features?.forEach { ComparisonFeatureRow(it) }
            }

            Column(
                modifier = Modifier
                    .matchParentSize()
                    // Keep the card's scroll to itself: at either end, the rest of a drag or fling
                    // must not move the sheet's content or drag the sheet closed
                    .nestedScroll(ConsumeRemainingScroll)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Dimens.space8)
            ) {
                selected.features.forEach { ComparisonFeatureRow(it) }
            }

            if (scrollState.canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0f), Color.White)))
                )
            }
        }
    }
}

@Composable
private fun ComparisonFeatureRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "✓",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = NunitoFontFamily,
            color = HeadingBlackColor.copy(alpha = 0.8f)
        )
    }
}
