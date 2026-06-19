package com.webscare.urdufonts.ui.detailScreen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.AppColor
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

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title = uiState.fontDetail?.name ?: "",
                onBackClick = onBackClick,
                onShareClick = onShareClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDownloadClick,
                containerColor = AppColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = "download button",
                )
            }
        }
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
                    onUnderlineToggle = viewModel::onUnderlineToggle
                )
            }
        }
    }
}

@Composable
private fun FontDetailContent(
    uiState: FontDetailUiState,
    onTabSelected: (DetailTab) -> Unit,
    onPreviewFontSizeChange: (Float) -> Unit,
    onBoldToggle: () -> Unit,
    onUnderlineToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detail = uiState.fontDetail ?: return
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val fontSectionRequester = remember { BringIntoViewRequester() }
    val previewSectionRequester = remember { BringIntoViewRequester() }
    val stylesSectionRequester = remember { BringIntoViewRequester() }
    val aboutSectionRequester = remember { BringIntoViewRequester() }

    fun requesterFor(tab: DetailTab) = when (tab) {
        DetailTab.FONT -> fontSectionRequester
        DetailTab.PREVIEW -> previewSectionRequester
        DetailTab.STYLES -> stylesSectionRequester
        DetailTab.ABOUT -> aboutSectionRequester
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DetailTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = { tab ->
                onTabSelected(tab)
                coroutineScope.launch {
                    requesterFor(tab).bringIntoView()
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)  // 👈 moved here
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .bringIntoViewRequester(fontSectionRequester)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                FontPreviewCard(previewText = detail.previewText)

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
                    .bringIntoViewRequester(previewSectionRequester)
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
                    .bringIntoViewRequester(stylesSectionRequester)
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
                    .bringIntoViewRequester(aboutSectionRequester)
            ) {
                SectionHeading(text = "About")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail.aboutText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = GreyColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = GreyColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = previewText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
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
        Text(
            text = "$weightsCount Weights",
            fontSize = 12.sp,
            color = GreyColor
        )
        Text(text = "|", fontSize = 12.sp, color = GreyColor.copy(alpha = 0.4f))
        MetadataChip(text = category)
        MetadataChip(text = fontFamily)
    }
}

@Composable
private fun MetadataChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColor.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = AppColor
        )
    }
}

@Composable
private fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier
) {
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
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = previewText,
                fontSize = fontSizePx.sp,
                fontWeight = if (isBoldEnabled) FontWeight.Bold else FontWeight.Normal,
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
            Text(
                text = "${fontSizePx.toInt()}px",
                fontSize = 12.sp,
                color = GreyColor
            )
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
            .clip(RoundedCornerShape(10.dp))
            .background(GreyColor.copy(alpha = 0.05f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = GreyColor
        )
        Text(
            text = urduText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = HeadingBlackColor
        )
    }
}