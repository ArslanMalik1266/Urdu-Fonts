package com.urdufonts.app.ui.subscription.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

@Composable
fun SubscriptionFeaturesSection(
    features: List<PremiumFeature>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PremiumFeaturesHeader()
        Spacer(modifier = Modifier.height(Dimens.space16))
        PremiumFeaturesGrid(features = features)
    }
}

@Composable
private fun PremiumFeaturesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            val w = size.width
            drawLine(
                color = Color(0xFFC2D9CD),
                start = Offset(0f, 0f),
                end = Offset(w - 6.dp.toPx(), 0f),
                strokeWidth = 1.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.width(Dimens.space8))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF86B39B))
        )

        Spacer(modifier = Modifier.width(Dimens.space8))

        Text(
            text = stringResource(id = R.string.subscription_premium_features),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = Color(0xFF0F4C2A)
        )

        Spacer(modifier = Modifier.width(Dimens.space8))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF86B39B))
        )

        Spacer(modifier = Modifier.width(Dimens.space8))

        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            val w = size.width
            drawLine(
                color = Color(0xFFC2D9CD),
                start = Offset(6.dp.toPx(), 0f),
                end = Offset(w, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun PremiumFeaturesGrid(features: List<PremiumFeature>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.space12),
        modifier = Modifier.fillMaxWidth()
    ) {
        val pairs = features.chunked(2)
        pairs.forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.space12)
            ) {
                rowFeatures.forEach { feature ->
                    Box(modifier = Modifier.weight(1f)) {
                        FeatureBadgeItem(feature = feature)
                    }
                }
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureBadgeItem(feature: PremiumFeature) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(Dimens.cornerMedium))
                .background(Color(0xFFEFF7F2)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = feature.iconRes),
                contentDescription = feature.title,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(DarkGreen)
            )
        }

        Spacer(modifier = Modifier.width(Dimens.space8))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = HeadingBlackColor,
                maxLines = 2
            )
            if (feature.subtitle.isNotBlank()) {
                Text(
                    text = feature.subtitle,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = NunitoFontFamily,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
            }
        }
    }
}
