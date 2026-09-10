package com.urdufonts.app.ui.subscription

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.ui.components.TopBarButton
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.theme.YellowColor
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: SubscriptionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val activity = context as? Activity

    uiState.userMessage?.let { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearUserMessage()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen background image
        Image(
            painter = painterResource(id = R.drawable.subscription_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Back Button Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TopBarButton(
                    iconRes = R.drawable.ic_back,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
            }

            if (!uiState.isLoading && uiState.options.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF7F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_error),
                                    contentDescription = "Error",
                                    modifier = Modifier.size(32.dp),
                                    colorFilter = ColorFilter.tint(DarkGreen)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Subscriptions Available",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily,
                                color = HeadingBlackColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "There are currently no active subscription plans available. Please check back later or retry.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = NunitoFontFamily,
                                color = GreyColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(44.dp)
                                    .addPressEffect(onClick = { viewModel.retryLoadData() })
                                    .softShadow(
                                        shadowColor = DarkGreen.copy(alpha = 0.2f),
                                        borderRadius = 22.dp,
                                        blurValue = 10.dp,
                                        offsetY = 0.dp
                                    )
                                    .clip(CircleShape)
                                    .background(DarkGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Retry",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NunitoFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Scrollable Subscription Form Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp, top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Logo Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        tonalElevation = 2.dp,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .softShadow(
                                    shadowColor = HeadingBlackColor.copy(alpha = 0.02f),
                                    borderRadius = 18.dp,
                                    blurValue = 20.dp,
                                    offsetY = 0.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.urdu_icon_splash),
                                contentDescription = "Urdu Logo",
                                modifier = Modifier
                                    .size(76.dp)
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Go Premium",
                        fontSize = 28.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = Color(0xFF0F4C2A)
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Unlock all premium features and\nenhance your font experience",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = HeadingBlackColor.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )

                    uiState.pendingChange?.let { pending ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_pass),
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plan change scheduled: ${pending.planName ?: pending.productSku ?: "Monthly"} taking effect on ${pending.effectiveAt ?: "next billing date"}.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = NunitoFontFamily,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.options.forEach { option ->
                            val isCurrentPlan = when (uiState.activePlanType) {
                                com.urdufonts.app.domain.models.SubscriptionType.MONTHLY -> option.id == "monthly-plan"
                                com.urdufonts.app.domain.models.SubscriptionType.SIX_MONTHS -> option.id == "6-month-plan"
                                com.urdufonts.app.domain.models.SubscriptionType.LIFETIME -> option.id == "urdu_fonts_lifetime" || option.id == "lifetime-purchase"
                                null -> false
                            }
                            SubscriptionOptionCard(
                                option = option,
                                isSelected = option.id == uiState.selectedOptionId,
                                isCurrentPlan = isCurrentPlan,
                                onClick = { viewModel.selectOption(option.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PremiumFeaturesHeader()

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumFeaturesGrid(features = uiState.features)

                    Spacer(modifier = Modifier.height(24.dp))

                    val buttonText = viewModel.getActionButtonText(uiState)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .addPressEffect(onClick = {
                                if (buttonText == "Manage Subscription") {
                                    viewModel.openManageSubscriptions(context)
                                } else if (activity != null && buttonText != "Lifetime Access Active") {
                                    viewModel.purchase(activity) {
                                        onNavigateToLogin()
                                    }
                                }
                            })
                            .softShadow(
                                shadowColor = DarkGreen.copy(alpha = 0.25f),
                                borderRadius = 27.dp,
                                blurValue = 14.dp,
                                offsetY = 4.dp
                            )
                            .clip(CircleShape)
                            .background(DarkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buttonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pass),
                                contentDescription = "Lock",
                                tint = GreyColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Secure Payment",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = NunitoFontFamily,
                                color = GreyColor.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = GreyColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Restore Purchases",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = DarkGreen,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.restorePurchases() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionOptionCard(
    option: SubscriptionOption,
    isSelected: Boolean,
    isCurrentPlan: Boolean,
    onClick: () -> Unit
) {
    val showTopBadge = isCurrentPlan || option.isMostPopular
    val displayPrice = remember(option.priceText) { option.priceText.removeSuffix(".00") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(
                shadowColor = HeadingBlackColor.copy(alpha = 0.04f),
                borderRadius = 16.dp,
                blurValue = 12.dp,
                offsetY = 2.dp
            )
            .addPressEffect(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = DarkGreen,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) DarkGreen else Color(0xFFD1D5DB),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DarkGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF7F2)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = option.iconRes),
                    contentDescription = option.title,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(DarkGreen)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor
                )

                Text(
                    text = option.billingPeriodText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = GreyColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                if (showTopBadge) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = displayPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor,
                    lineHeight = 16.sp
                )

                if (option.pricePeriodSubtitle != null) {
                    Text(
                        text = option.pricePeriodSubtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = GreyColor,
                        lineHeight = 11.sp
                    )
                }

                if (option.discountTag != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2F3E7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = option.discountTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = DarkGreen
                        )
                    }
                }
            }
        }

        if (showTopBadge) {
            val badgeText = if (isCurrentPlan) "Current Plan" else "Most Popular"
            val badgeBgColor = if (isCurrentPlan) YellowColor else DarkGreen
            val badgeTextColor = if (isCurrentPlan) HeadingBlackColor else Color.White

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 10.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isCurrentPlan) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_premium),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = badgeTextColor
                    )
                }
            }
        }
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

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF86B39B))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Premium Features",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = Color(0xFF0F4C2A)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF86B39B))
        )

        Spacer(modifier = Modifier.width(8.dp))

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val pairs = features.chunked(2)
        pairs.forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEFF7F2)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = feature.iconRes),
                contentDescription = feature.title,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(DarkGreen)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = feature.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = HeadingBlackColor
            )

            Text(
                text = feature.subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily,
                color = GreyColor
            )
        }
    }
}
