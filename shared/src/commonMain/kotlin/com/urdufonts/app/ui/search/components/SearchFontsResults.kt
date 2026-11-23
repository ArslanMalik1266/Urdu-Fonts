package com.urdufonts.app.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.ui.components.FontItemCard
import com.urdufonts.app.ui.util.springOverscroll

@Composable
fun SearchFontsResults(
    fonts: List<FontItem>,
    onFontClick: (FontItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .springOverscroll(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(fonts, key = { it.id }) { font ->
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                FontItemCard(
                    fontItem = font,
                    onFontClick = { onFontClick(font) },
                    onDownloadClick = { onFontClick(font) }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
