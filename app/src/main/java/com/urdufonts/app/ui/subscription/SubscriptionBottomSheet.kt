package com.urdufonts.app.ui.subscription

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.ui.subscription.components.EmptySubscriptionView
import com.urdufonts.app.ui.subscription.components.PendingChangeBanner
import com.urdufonts.app.ui.subscription.components.SubscriptionFooter
import com.urdufonts.app.ui.subscription.components.SubscriptionOptionCard
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: SubscriptionViewModel = koinViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val activity = context as? Activity
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onScreenOpened()
    }

    uiState.userMessage?.let { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearUserMessage()
    }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = Dimens.cornerLarge, topEnd = Dimens.cornerLarge))
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.subscription_background),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = Dimens.space24)
                    .padding(bottom = Dimens.space12),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.space12, bottom = Dimens.space12)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB))
                )

                if (!uiState.isLoading && uiState.options.isEmpty()) {
                    EmptySubscriptionView(onRetry = { viewModel.retryLoadData() })
                } else {
                    Text(
                        text = stringResource(id = R.string.subscription_title),
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = Color(0xFF0F4C2A)
                    )

                    Spacer(modifier = Modifier.height(Dimens.space4))

                    Text(
                        text = stringResource(id = R.string.subscription_subtitle),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = HeadingBlackColor.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimens.space8))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.space32),
                        contentAlignment = Alignment.Center
                    ) {
                        uiState.pendingChange?.let { pending ->
                            PendingChangeBanner(
                                pendingPlanName = pending.planName ?: pending.productSku ?: "",
                                effectiveAt = pending.effectiveAt
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.space8))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.space12),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.options.forEach { option ->
                            val isCurrentPlan = uiState.activePlanType?.sku == option.id
                            val isPendingTarget = uiState.isPendingTarget(option.id)

                            SubscriptionOptionCard(
                                option = option,
                                isSelected = option.id == uiState.selectedOptionId,
                                isCurrentPlan = isCurrentPlan,
                                isPendingTargetPlan = isPendingTarget,
                                onClick = { viewModel.selectOption(option.id) }
                            )
                        }
                    }

                    // What the selected plan gives: its entitlement names from the IAP dashboard
                    uiState.options.firstOrNull { it.id == uiState.selectedOptionId }?.let { selected ->
                        if (selected.features.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Dimens.space24))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.cornerLarge))
                                    .background(Color.White)
                                    .padding(Dimens.space16),
                                verticalArrangement = Arrangement.spacedBy(Dimens.space8)
                            ) {
                                Text(
                                    text = "What you get with ${selected.title}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NunitoFontFamily,
                                    color = HeadingBlackColor
                                )
                                selected.features.forEach { feature ->
                                    com.urdufonts.app.ui.subscription.components.PlanFeatureRow(feature)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.space24))

                    SubscriptionFooter(
                        buttonText = uiState.actionButtonText,
                        enabled = uiState.isActionEnabled,
                        onActionClick = {
                            if (uiState.isPurchasing) return@SubscriptionFooter
                            when (uiState.actionType) {
                                SubscriptionActionType.MANAGE_EXTERNAL -> {
                                    val activeSku = when (uiState.activePlanType) {
                                        SubscriptionType.LIFETIME -> "urdu_fonts_lifetime"
                                        null -> null
                                        else -> "urdu_fonts_pro"
                                    }
                                    val url = if (activeSku != null) {
                                        "https://play.google.com/store/account/subscriptions?sku=$activeSku&package=${context.packageName}"
                                    } else {
                                        "https://play.google.com/store/account/subscriptions?package=${context.packageName}"
                                    }
                                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                                }
                                SubscriptionActionType.SUBSCRIBE,
                                SubscriptionActionType.UPGRADE,
                                SubscriptionActionType.DOWNGRADE -> {
                                    if (activity != null) {
                                        viewModel.purchase(activity) { onNavigateToLogin() }
                                    }
                                }
                                else -> Unit
                            }
                        },
                        onRestoreClick = { viewModel.restorePurchases() }
                    )
                }
            }
        }
    }
}
