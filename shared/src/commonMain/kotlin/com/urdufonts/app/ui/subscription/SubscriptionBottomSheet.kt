package com.urdufonts.app.ui.subscription

import com.urdufonts.app.resources.*
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
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.ui.subscription.components.EmptySubscriptionView
import com.urdufonts.app.ui.subscription.components.PendingChangeBanner
import com.urdufonts.app.ui.subscription.components.PlanComparisonCard
import com.urdufonts.app.ui.subscription.components.SubscriptionFooter
import com.urdufonts.app.ui.subscription.components.SubscriptionOptionCard
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

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
    val activity = com.urdufonts.app.ui.platform.rememberPlatformActivity()
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onScreenOpened()
    }

    uiState.userMessage?.let { msg ->
        com.urdufonts.app.ui.platform.showToast(msg)
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
                painter = painterResource(Res.drawable.subscription_background),
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
                    // Takes the 24dp removed above the plan cards, so the sheet keeps its height
                    Spacer(modifier = Modifier.height(Dimens.space24))

                    Text(
                        text = stringResource(Res.string.subscription_title),
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = Color(0xFF0F4C2A)
                    )

                    Spacer(modifier = Modifier.height(Dimens.space4))

                    Text(
                        text = stringResource(Res.string.subscription_subtitle),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = HeadingBlackColor.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )

                    // Same gap as between the plan cards and the features card
                    Spacer(modifier = Modifier.height(Dimens.space24))

                    uiState.pendingChange?.let { pending ->
                        PendingChangeBanner(
                            pendingPlanName = pending.planName ?: pending.productSku ?: "",
                            effectiveAt = pending.effectiveAt
                        )
                        Spacer(modifier = Modifier.height(Dimens.space12))
                    }

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

                    // The selected plan against all plans; fixed height, so the sheet never resizes
                    uiState.options.firstOrNull { it.id == uiState.selectedOptionId }?.let { selected ->
                        if (uiState.options.any { it.features.isNotEmpty() }) {
                            Spacer(modifier = Modifier.height(Dimens.space24))
                            PlanComparisonCard(options = uiState.options, selected = selected)
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
                                    val url = com.urdufonts.app.ui.platform.manageSubscriptionsUrl(activeSku)
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
