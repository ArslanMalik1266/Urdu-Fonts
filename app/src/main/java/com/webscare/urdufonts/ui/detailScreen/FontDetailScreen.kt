package com.webscare.urdufonts.ui.detailScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.ui.components.AnimatedDownloadButton
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.CustomSlider
import com.webscare.urdufonts.ui.util.ShimmerBox
import com.webscare.urdufonts.ui.util.addPressEffect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.theme.YellowColor
import com.webscare.urdufonts.ui.util.springOverscroll
import kotlinx.coroutines.flow.collectLatest

private const val DEFAULT_PREVIEW_TEXT = "بہتر کل کی امید اور کامل یقین"
private val URDU_SAMPLE_TEXTS = listOf(
    "بہتر کل کی امید",
    "اللہ اکبر",
    "پاکستان زندہ باد",
    "محبت کی راہ میں"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontDetailScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    viewModel: FontDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastScrollValue by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    // 🟢 Listen to ViewModel events (Show Snackbar when download completes or fails)
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FontDetailViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }
    var isExpanded by remember { mutableStateOf(true) }
    val fontFamily by viewModel.fontFamilyState.collectAsStateWithLifecycle()
    val fontWeights by viewModel.fontWeightsState.collectAsStateWithLifecycle()
    val selectedWeightIndex by viewModel.selectedWeightIndex.collectAsStateWithLifecycle()
    val initialFontFamily by viewModel.initialFontFamily.collectAsStateWithLifecycle()

    // ── Consolidate loading logic: Ready only when API details + Weights + Font Files are loaded ──
    val isContentReady = uiState.fontDetail != null &&
            initialFontFamily != null &&
            fontWeights.isNotEmpty()

    LaunchedEffect(scrollState.value) {
        val currentScroll = scrollState.value
        when {
            currentScroll == 0 -> isExpanded = true
            currentScroll > lastScrollValue -> isExpanded = false
            currentScroll < lastScrollValue -> isExpanded = true
        }
        lastScrollValue = currentScroll
    }

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title = uiState.fontDetail?.name ?: "",
                onBackClick = onBackClick,
                onShareClick = onShareClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val parts = data.visuals.message.split("|")
                val title = parts.getOrNull(0) ?: data.visuals.message
                val folderPath = parts.getOrNull(1)

                androidx.compose.material3.Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color.White, // Rich dark theme background
                    contentColor = HeadingBlackColor,
                    shape = RoundedCornerShape(12.dp),
                    action = {
                        Text(
                            text = "OK",
                            color = AppColor, // App Green Theme Action
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = NunitoFontFamily,
                            modifier = Modifier
                                .clickable { data.dismiss() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AppColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_tick),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(AppColor),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Column {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily,
                                color = HeadingBlackColor
                            )
                            if (!folderPath.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Saved in: $folderPath",
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFontFamily,
                                    color = HeadingBlackColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {

        // Show download FAB only when content is ready
            if (uiState.errorMessage == null && isContentReady) {
                AnimatedDownloadButton(
                    downloadState = uiState.downloadState, // 🟢 Passed download state
                    progress = uiState.downloadProgress,   // 🟢 Passed progress value
                    isExpanded = isExpanded,
                    onClick = { viewModel.downloadFont() } // 🟢 Updated click action
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Crossfade(
            targetState = when {
                uiState.errorMessage != null -> "error"
                isContentReady -> "content"
                else -> "loading"
            },
            animationSpec = tween(durationMillis = 350),
            label = "detail_screen_transition"
        ) { state ->
            when (state) {
                "loading" -> {
                    FontDetailSkeleton(
                        innerPadding = innerPadding,
                        selectedTab = uiState.selectedTab,
                        onTabSelected = viewModel::onTabSelected
                    )
                }
                "error" -> {
                    OfflineErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White)
                    )
                }
                "content" -> {
                    FontDetailContent(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        fontFamily = fontFamily,
                        fontWeights = fontWeights,
                        initialFontFamily = initialFontFamily,
                        selectedWeightIndex = selectedWeightIndex,
                        onWeightSelected = viewModel::onWeightSelected,
                        onTabSelected = viewModel::onTabSelected,
                        onPreviewFontSizeChange = viewModel::onPreviewFontSizeChange,
                        onBoldToggle = viewModel::onBoldToggle,
                        onUnderlineToggle = viewModel::onUnderlineToggle,
                        scrollState = scrollState
                    )
                }
            }
        }
    }
}

// ─── Visible fraction helper ─────────────────────────────────────────────────
private fun visibleFraction(
    sectionY: Float,
    sectionH: Float,
    scrollY: Float,
    screenH: Float
): Float {
    if (sectionH == 0f) return 0f
    val top = sectionY - scrollY
    val bottom = top + sectionH
    val visibleTop = maxOf(top, 0f)
    val visibleBottom = minOf(bottom, screenH)
    return if (visibleBottom > visibleTop) (visibleBottom - visibleTop) / sectionH else 0f
}

@Composable
private fun FontDetailContent(
    uiState: FontDetailUiState,
    fontFamily: FontFamily?,
    initialFontFamily: FontFamily?,
    fontWeights: List<Pair<String, FontFamily>>,
    selectedWeightIndex: Int,
    onWeightSelected: (Int) -> Unit,
    onTabSelected: (DetailTab) -> Unit,
    onPreviewFontSizeChange: (Float) -> Unit,
    onBoldToggle: () -> Unit,
    scrollState: ScrollState,
    onUnderlineToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detail = uiState.fontDetail ?: return
    val coroutineScope = rememberCoroutineScope()

    var userSelectedTab by remember { mutableStateOf<DetailTab?>(null) }
    var isAboutExpanded by remember { mutableStateOf(false) }
    var isUserScrolling by remember { mutableStateOf(false) }

    var fontSectionY by remember { mutableStateOf(0f) }
    var fontSectionH by remember { mutableStateOf(0f) }
    var previewSectionY by remember { mutableStateOf(0f) }
    var previewSectionH by remember { mutableStateOf(0f) }
    var stylesSectionY by remember { mutableStateOf(0f) }
    var stylesSectionH by remember { mutableStateOf(0f) }
    var aboutSectionY by remember { mutableStateOf(0f) }
    var aboutSectionH by remember { mutableStateOf(0f) }
    var infoSectionY by remember { mutableStateOf(0f) }
    var infoSectionH by remember { mutableStateOf(0f) }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeightDp.toPx() }
    val scrollY = scrollState.value.toFloat()

    val fractions = mapOf(
        DetailTab.FONT to visibleFraction(fontSectionY, fontSectionH, scrollY, screenHeightPx),
        DetailTab.PREVIEW to visibleFraction(
            previewSectionY,
            previewSectionH,
            scrollY,
            screenHeightPx
        ),
        DetailTab.STYLES to visibleFraction(
            stylesSectionY,
            stylesSectionH,
            scrollY,
            screenHeightPx
        ),
        DetailTab.ABOUT to visibleFraction(aboutSectionY, aboutSectionH, scrollY, screenHeightPx),
        DetailTab.INFO to visibleFraction(infoSectionY, infoSectionH, scrollY, screenHeightPx),
    )

    val scrollDerivedTab = fractions.maxByOrNull { it.value }?.key ?: DetailTab.FONT
    val activeTab = userSelectedTab ?: scrollDerivedTab

    var lastScrollValue by remember { mutableStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val current = scrollState.value
        if (!isUserScrolling && current != lastScrollValue) {
            userSelectedTab = null
        }
        lastScrollValue = current
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && isUserScrolling) {
            isUserScrolling = false
        }
    }

    LaunchedEffect(activeTab) {
        onTabSelected(activeTab)
    }

    var previewText by remember { mutableStateOf(DEFAULT_PREVIEW_TEXT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DetailTabRow(
            selectedTab = activeTab,
            onTabSelected = { tab ->
                onTabSelected(tab)
                userSelectedTab = tab
                isUserScrolling = true
                isAboutExpanded = (tab == DetailTab.ABOUT)
                coroutineScope.launch {
                    val target = when (tab) {
                        DetailTab.FONT -> 0
                        DetailTab.PREVIEW -> previewSectionY.toInt()
                        DetailTab.STYLES -> stylesSectionY.toInt()
                        DetailTab.ABOUT -> aboutSectionY.toInt()
                        DetailTab.INFO -> infoSectionY.toInt()
                    }
                    scrollState.animateScrollTo(target.coerceAtLeast(0))
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .springOverscroll()
        ) {
            // ── FONT section ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        fontSectionY = coords.positionInParent().y
                        fontSectionH = coords.size.height.toFloat()
                    }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                FontPreviewCard(
                    fontFamily = initialFontFamily,
                    previewText = previewText,
                    fontSizePx = uiState.previewFontSizePx,
                    isBoldEnabled = uiState.isBoldEnabled,
                    isUnderlineEnabled = uiState.isUnderlineEnabled,
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetadataRow(
                    fontWeight = detail.weightCount.toString(),
                    categories = detail.categories ?: emptyList()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── PREVIEW section ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        previewSectionY = coords.positionInParent().y
                        previewSectionH = coords.size.height.toFloat()
                    }
            ) {
                SectionHeading(text = "Preview")
                Spacer(modifier = Modifier.height(8.dp))
                PreviewControlsSection(
                    previewText = previewText,
                    onPreviewTextChange = { previewText = it },
                    fontSizePx = uiState.previewFontSizePx,
                    fontFamily = fontFamily,
                    isBoldEnabled = uiState.isBoldEnabled,
                    isUnderlineEnabled = uiState.isUnderlineEnabled,
                    onFontSizeChange = onPreviewFontSizeChange,
                    onBoldToggle = onBoldToggle,
                    onUnderlineToggle = onUnderlineToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── WEIGHTS section ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        stylesSectionY = coords.positionInParent().y
                        stylesSectionH = coords.size.height.toFloat()
                    }
            ) {
                SectionHeading(text = "Weights")
                Spacer(modifier = Modifier.height(12.dp))

                if (fontWeights.isEmpty()) {
                    // Shimmer while weights are loading
                    repeat(3) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    fontWeights.forEachIndexed { index, (weightName, weightFamily) ->
                        val isSelected = index == selectedWeightIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .addPressEffect {
                                    onWeightSelected(index)
                                }
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    GreyColor.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = weightName,
                                fontSize = 13.sp,
                                color = GreyColor,
                                fontWeight = FontWeight.Normal,
                                fontFamily = NunitoFontFamily
                            )
                            Text(
                                text = previewText,
                                fontSize = 16.sp,
                                fontFamily = weightFamily,
                                color = HeadingBlackColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── ABOUT section ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        aboutSectionY = coords.positionInParent().y
                        aboutSectionH = coords.size.height.toFloat()
                    }
            ) {
                AboutSection(
                    aboutText = detail.description,
                    isExpanded = isAboutExpanded,
                    onExpandedChange = { isAboutExpanded = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── INFO section ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        infoSectionY = coords.positionInParent().y
                        infoSectionH = coords.size.height.toFloat()
                    }
            ) {
                InfoSection(detail = detail)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// Internal accessibility so FontDetailSkeleton.kt can draw it as a static anchor
@Composable
internal fun DetailTabRow(
    selectedTab: DetailTab,
    onTabSelected: (DetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tab.label,
                    fontSize = 14.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) HeadingBlackColor else GreyColor,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(AppColor, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun FontPreviewCard(
    previewText: String,
    fontSizePx: Float,
    fontFamily: FontFamily?,
    isBoldEnabled: Boolean,
    isUnderlineEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardScrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = GreyColor.copy(alpha = 0.06f))
            .verticalScroll(cardScrollState)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (fontFamily == null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                )
            }
        } else {
            Text(
                text = previewText,
                fontSize = fontSizePx.sp,
                fontFamily = fontFamily,
                fontWeight = if (isBoldEnabled) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (isUnderlineEnabled) TextDecoration.Underline else null,
                color = HeadingBlackColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MetadataRow(
    fontWeight: String,
    categories: List<FontClassifier>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Weight: $fontWeight",
            fontSize = 12.sp,
            color = GreyColor,
            fontFamily = NunitoFontFamily
        )
        Text(text = "|", fontSize = 12.sp, color = GreyColor.copy(alpha = 0.4f), fontFamily = NunitoFontFamily)
        categories.forEach { category ->
            MetadataChip(text = category.title)
        }
    }
}

@Composable
private fun MetadataChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .addPressEffect()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColor.copy(alpha = 0.05f))
            .border(BorderStroke(0.5.dp, AppColor.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = HeadingBlackColor,
            fontFamily = NunitoFontFamily
        )
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = GreyColor,
        modifier = modifier,
        fontFamily = NunitoFontFamily
    )
}

@Composable
private fun PreviewControlsSection(
    previewText: String,
    onPreviewTextChange: (String) -> Unit,
    fontSizePx: Float,
    fontFamily: FontFamily?,
    isBoldEnabled: Boolean,
    isUnderlineEnabled: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onBoldToggle: () -> Unit,
    onUnderlineToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {

        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColor.copy(alpha = 0.07f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Go to Settings → Language → Add Urdu keyboard",
                        fontSize = 11.sp,
                        color = AppColor,
                        lineHeight = 16.sp,
                        fontFamily = NunitoFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ── Editable text field ──────────────────────────────────────
        val focusManager = LocalFocusManager.current

        BasicTextField(
            value = previewText,
            onValueChange = onPreviewTextChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Normal,
                textDecoration = TextDecoration.None,
                color = GreyColor.copy(0.5f),
                textAlign = TextAlign.Right,
                lineHeight = 28.sp
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isFocused) AppColor.copy(alpha = 0.04f)
                    else GreyColor.copy(alpha = 0.06f)
                )
                .border(
                    width = 1.dp,
                    color = if (isFocused) AppColor.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (previewText.isEmpty()) {
                        Text(
                            text = "یہاں لکھیں...",
                            fontSize = 16.sp,
                            color = GreyColor.copy(alpha = 0.4f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Size slider + bold/underline toggles ─────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "${fontSizePx.toInt()}px", fontSize = 12.sp, color = GreyColor, fontFamily = NunitoFontFamily)
            Spacer(modifier = Modifier.width(8.dp))
            CustomSlider(
                value = fontSizePx,
                onValueChange = onFontSizeChange,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StyleToggleIcon(
                iconRes = R.drawable.ic_bold,
                isActive = isBoldEnabled,
                onClick = onBoldToggle
            )
            Spacer(modifier = Modifier.width(8.dp))
            StyleToggleIcon(
                iconRes = R.drawable.ic_underline,
                isActive = isUnderlineEnabled,
                onClick = onUnderlineToggle
            )
        }
    }
}

@Composable
private fun StyleToggleIcon(
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) YellowColor.copy(0.8f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (isActive) HeadingBlackColor else GreyColor),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun AboutSection(
    aboutText: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var cutIndex by remember { mutableStateOf<Int?>(null) }

    val plainText = remember(aboutText) {
        AnnotatedString.fromHtml(aboutText).text
            .trimEnd()
            .replace(Regex("\\n+$"), "")
            .trimEnd()
    }
    val displayText = buildAnnotatedString {
        if (isExpanded) {
            append(plainText)
            withStyle(
                SpanStyle(
                    color = AppColor,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp
                )
            ) {
                append("  see less")
            }
        } else {
            val cut = cutIndex
            if (cut != null) {
                val trimmed = plainText.take(cut).trimEnd().trimEnd { !it.isWhitespace() }.trimEnd()
                append(trimmed)
                withStyle(
                    SpanStyle(
                        color = AppColor,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp
                    )
                ) {
                    append("... see more")
                }
            } else {
                append(plainText)
            }
        }
    }

    Column(modifier = modifier
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )) {
        SectionHeading(text = "About")
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayText,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = GreyColor,
            fontFamily = NunitoFontFamily,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Clip,
            onTextLayout = { result ->
                if (!isExpanded && cutIndex == null && result.hasVisualOverflow) {
                    val lastLine = result.lineCount - 1
                    val lineEnd = result.getLineEnd(lastLine, visibleEnd = true)
                    cutIndex = (lineEnd - 25).coerceAtLeast(0)
                }
            },
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onExpandedChange(!isExpanded) }
        )
    }
}

@Composable
private fun InfoSection(detail: FontItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionHeading(text = "Info")
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GreyColor.copy(alpha = 0.05f))
                .padding(vertical = 4.dp)
        ) {
            Column {
                InfoRow(
                    iconRes = R.drawable.ic_weights,
                    label = "Weight",
                    value = detail.weightCount.toString()
                )
                InfoDivider()
                InfoRow(
                    iconRes = R.drawable.ic_language,
                    label = "Language",
                    value = detail.language
                )
                InfoDivider()
                InfoRow(
                    iconRes = R.drawable.ic_developer,
                    label = "Font Developer",
                    value = detail.developer
                )
                InfoDivider()
                InfoRow(
                    iconRes = R.drawable.ic_font_family,
                    label = "Font Family",
                    value = detail.fontFamily
                )
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_format, label = "Format", value = "TTF")
            }
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    label: String,
    value: String,
    valueColor: Color = DarkGreen,
    valueFontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GreyColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(18.dp)
            )
            Text(text = label, fontSize = 14.sp, color = GreyColor, fontFamily = NunitoFontFamily)
        }
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = valueFontWeight,
            fontFamily = NunitoFontFamily
        )
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = GreyColor.copy(alpha = 0.15f)
    )
}

@Composable
private fun OfflineErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNoInternet = message.contains("internet", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AppColor.copy(alpha = 0.12f))
                        .alpha(if (isNoInternet) pulseAlpha else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isNoInternet) R.drawable.ic_no_wifi
                            else R.drawable.ic_error
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(AppColor),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isNoInternet) "You're Offline" else "Something Went Wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isNoInternet)
                    "Check your connection and tap retry to load the font."
                else message,
                fontSize = 14.sp,
                color = GreyColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontFamily = NunitoFontFamily
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRetry
                    )
                    .padding(horizontal = 36.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontFamily = NunitoFontFamily
                )
            }
        }
    }
}
