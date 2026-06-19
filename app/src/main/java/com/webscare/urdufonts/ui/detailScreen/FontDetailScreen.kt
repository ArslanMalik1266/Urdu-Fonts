package com.webscare.urdufonts.ui.detailScreen

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.FontDetail
import com.webscare.urdufonts.ui.components.AnimatedDownloadButton
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.util.CustomSlider
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    var isExpanded by remember { mutableStateOf(true) }

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
        floatingActionButton = {
            AnimatedDownloadButton(
                isExpanded = isExpanded,
                onClick = onDownloadClick
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.fontDetail != null -> {
                FontDetailContent(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
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

// ─── Visible fraction helper ────────────────────────────────────────────────
// Returns 0..1: how much of the section is currently visible on screen
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
        DetailTab.FONT    to visibleFraction(fontSectionY,    fontSectionH,    scrollY, screenHeightPx),
        DetailTab.PREVIEW to visibleFraction(previewSectionY, previewSectionH, scrollY, screenHeightPx),
        DetailTab.STYLES  to visibleFraction(stylesSectionY,  stylesSectionH,  scrollY, screenHeightPx),
        DetailTab.ABOUT   to visibleFraction(aboutSectionY,   aboutSectionH,   scrollY, screenHeightPx),
        DetailTab.INFO    to visibleFraction(infoSectionY,    infoSectionH,    scrollY, screenHeightPx),
    )

    val scrollDerivedTab = fractions.maxByOrNull { it.value }?.key ?: DetailTab.FONT

    // ✅ User tap overrides scroll spy; null means scroll spy is in control
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
            // keep userSelectedTab locked
        }
    }

    LaunchedEffect(activeTab) {
        onTabSelected(activeTab)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DetailTabRow(
            selectedTab = activeTab,
            onTabSelected = { tab ->
                onTabSelected(tab)
                userSelectedTab = tab       // ✅ lock immediately on tap
                isUserScrolling = true      // ✅ mark programmatic scroll started
                coroutineScope.launch {
                    val target = when (tab) {
                        DetailTab.FONT    -> 0
                        DetailTab.PREVIEW -> previewSectionY.toInt()
                        DetailTab.STYLES  -> stylesSectionY.toInt()
                        DetailTab.ABOUT   -> aboutSectionY.toInt()
                        DetailTab.INFO    -> infoSectionY.toInt()
                    }
                    scrollState.animateScrollTo(target.coerceAtLeast(0))
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
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
                    previewText = detail.previewText,
                    fontSizePx = uiState.previewFontSizePx,
                    isBoldEnabled = uiState.isBoldEnabled,
                    isUnderlineEnabled = uiState.isUnderlineEnabled
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetadataRow(
                    weightsCount = detail.weightsCount,
                    category = detail.category,
                    fontFamily = detail.fontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    previewText = detail.previewText,
                    fontSizePx = uiState.previewFontSizePx,
                    isBoldEnabled = uiState.isBoldEnabled,
                    isUnderlineEnabled = uiState.isUnderlineEnabled,
                    onFontSizeChange = onPreviewFontSizeChange,
                    onBoldToggle = onBoldToggle,
                    onUnderlineToggle = onUnderlineToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        stylesSectionY = coords.positionInParent().y
                        stylesSectionH = coords.size.height.toFloat()
                    }
            ) {
                SectionHeading(text = "Styles")
                Spacer(modifier = Modifier.height(8.dp))
                detail.weightSamples.forEach { sample ->
                    WeightSampleRow(label = sample.label, urduText = sample.urduText)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coords ->
                        aboutSectionY = coords.positionInParent().y
                        aboutSectionH = coords.size.height.toFloat()
                    }
            ) {
                AboutSection(aboutText = detail.aboutText)
            }

            Spacer(modifier = Modifier.height(24.dp))

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

@Composable
private fun DetailTabRow(
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
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) AppColor else GreyColor,
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
    isBoldEnabled: Boolean,
    isUnderlineEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = GreyColor.copy(alpha = 0.06f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = previewText,
            fontSize = fontSizePx.sp,
            fontWeight = if (isBoldEnabled) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (isUnderlineEnabled) TextDecoration.Underline else null,
            color = HeadingBlackColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MetadataRow(
    weightsCount: Int,
    category: String,
    fontFamily: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "$weightsCount Weights", fontSize = 12.sp, color = GreyColor)
        Text(text = "|", fontSize = 12.sp, color = GreyColor.copy(alpha = 0.4f))
        MetadataChip(text = category)
        MetadataChip(text = fontFamily)
    }
}

@Composable
private fun MetadataChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppColor.copy(alpha = 0.05f))
            .border(BorderStroke(0.5.dp, AppColor.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = AppColor)
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = HeadingBlackColor,
        modifier = modifier
    )
}

@Composable
private fun PreviewControlsSection(
    previewText: String,
    fontSizePx: Float,
    isBoldEnabled: Boolean,
    isUnderlineEnabled: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onBoldToggle: () -> Unit,
    onUnderlineToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GreyColor.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = previewText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = GreyColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "${fontSizePx.toInt()}px", fontSize = 12.sp, color = GreyColor)
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
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) AppColor.copy(alpha = 0.15f) else Color.Transparent)
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
            colorFilter = ColorFilter.tint(if (isActive) AppColor else GreyColor),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun WeightSampleRow(
    label: String,
    urduText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(GreyColor.copy(alpha = 0.05f))
                .padding(12.dp)
        ) {
            Text(text = label, fontSize = 13.sp, color = GreyColor)
        }
        Text(text = urduText, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = HeadingBlackColor)
    }
}

@Composable
private fun AboutSection(aboutText: String, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        SectionHeading(text = "About")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = aboutText,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = GreyColor,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) hasOverflow = result.hasVisualOverflow
            }
        )
        if (hasOverflow || isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) "see less" else "see more",
                fontSize = 13.sp,
                color = AppColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
private fun InfoSection(detail: FontDetail, modifier: Modifier = Modifier) {
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
                InfoRow(iconRes = R.drawable.ic_weights,     label = "Weights",        value = detail.weightsCount.toString())
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_language,    label = "Language",       value = detail.language)
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_developer,   label = "Font Developer", value = detail.developer)
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_font_family, label = "Font Family",    value = detail.fontFamily)
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_file_size,   label = "File Size",      value = detail.fileSize)
                InfoDivider()
                InfoRow(iconRes = R.drawable.ic_format,      label = "Format",         value = detail.format)
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
            Text(text = label, fontSize = 14.sp, color = GreyColor)
        }
        Text(text = value, fontSize = 14.sp, color = valueColor, fontWeight = valueFontWeight)
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