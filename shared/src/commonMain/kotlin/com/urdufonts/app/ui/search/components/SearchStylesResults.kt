package com.urdufonts.app.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.urdufonts.app.domain.models.StyleItem
import com.urdufonts.app.ui.style.StyleItemCard
import com.urdufonts.app.ui.util.springOverscroll

@Composable
fun SearchStylesResults(
    styles: List<StyleItem>,
    onStyleClick: (StyleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .springOverscroll(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(styles, key = { it.id }) { style ->
            StyleItemCard(
                style = style,
                onClick = { onStyleClick(style) }
            )
        }
    }
}
