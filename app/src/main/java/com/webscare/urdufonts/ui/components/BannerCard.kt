package com.webscare.urdufonts.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.webscare.urdufonts.domain.models.BannerItem

@Composable
fun BannerCard(
    bannerItem: BannerItem,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = bannerItem.image),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth(),
        contentScale = ContentScale.Crop
    )
}