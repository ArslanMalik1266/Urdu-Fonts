package com.urdufonts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect

@Composable
fun FontItemCard(
    fontItem: FontItem,
    onDownloadClick: (FontItem) -> Unit,
    onFontClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .addPressEffect { onFontClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = 12.dp,
                    bottom = 24.dp,
                    start = 8.dp,
                    end = 8.dp
                )
        ) {
            FontItemHeader(fontItem = fontItem)
            FontItemPreview(fontItem = fontItem, onDownloadClick = onDownloadClick)
        }
    }
}

@Composable
private fun FontItemHeader(fontItem: FontItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = fontItem.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = HeadingBlackColor,
            modifier = Modifier.weight(1f),
            fontFamily = NunitoFontFamily
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fontItem.primaryStyleName,
                fontSize = 10.sp,
                color = GreyColor,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "·",
                fontSize = 10.sp,
                color = GreyColor,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = fontItem.primaryCategoryName,
                fontSize = 10.sp,
                color = GreyColor,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "|",
                fontSize = 10.sp,
                color = GreyColor,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "Weights: ${fontItem.weightCount}",
                fontSize = 10.sp,
                color = GreyColor,
                fontWeight = FontWeight.Medium,
                fontFamily = NunitoFontFamily
            )
        }
    }
}

@Composable
private fun FontItemPreview(
    fontItem: FontItem,
    onDownloadClick: (FontItem) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DownloadBox(
            onDownloadClick = false,
            onClick = { onDownloadClick(fontItem) }
        )
        AsyncImage(
            model = remember(fontItem.featureImageUrl) {
                ImageRequest.Builder(context)
                    .data(fontItem.featureImageUrl)
                    .crossfade(true)
                    .crossfade(300)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
            },
            contentDescription = fontItem.name,
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            alignment = Alignment.CenterEnd
        )
    }
}