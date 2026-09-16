package com.urdufonts.app.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.search.SearchTab
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    fontCount: Int,
    styleCount: Int,
    categoryCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(SearchTab.FONTS, SearchTab.STYLES, SearchTab.CATEGORIES)

    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = DarkGreen,
        divider = {
            HorizontalDivider(
                color = Color(0xFFF0F3F1),
                thickness = 1.dp
            )
        },
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                height = 3.dp,
                color = DarkGreen
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.space16)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            val count = when (tab) {
                SearchTab.FONTS -> fontCount
                SearchTab.STYLES -> styleCount
                SearchTab.CATEGORIES -> categoryCount
            }

            val labelText = "${tab.label} ($count)"

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = if (isSelected) DarkGreen else HeadingBlackColor.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            )
        }
    }
}
