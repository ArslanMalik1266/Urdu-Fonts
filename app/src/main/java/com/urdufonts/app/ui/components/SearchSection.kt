package com.urdufonts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect // Imported your custom press effect modifier
import com.urdufonts.app.ui.util.softShadow

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun CustomSearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholderText: String = "Search fonts, categories, styles...",
    focusRequester: FocusRequester? = null,
    onClick: (() -> Unit)? = null,
    onDone: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) AppColor.copy(alpha = 0.3f) else GreyColor.copy(alpha = 0.2f)

    val rowModifier = if (readOnly && onClick != null) {
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .softShadow(
                shadowColor = HeadingBlackColor.copy(0.02f),
                offsetY = (0).dp,
                blurValue = 6.dp,
                borderRadius = 999.dp
            )
            .clip(CircleShape)
            .background(Color.White)
            .border(0.5.dp, borderColor, CircleShape)
            .addPressEffect { onClick() }
            .padding(horizontal = 14.dp)
    } else {
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .softShadow(
                shadowColor = HeadingBlackColor.copy(0.02f),
                offsetY = (0).dp,
                blurValue = 6.dp,
                borderRadius = 999.dp
            )
            .clip(CircleShape)
            .background(Color.White)
            .border(0.5.dp, borderColor, CircleShape)
            .padding(horizontal = 14.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (readOnly) {
            Text(
                text = if (query.isEmpty()) placeholderText else query,
                color = if (query.isEmpty()) GreyColor.copy(alpha = 0.6f) else HeadingBlackColor,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.weight(1f)
            )
        } else {
            val textModifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused }
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = textModifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            ) { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        placeholderText,
                        color = GreyColor.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontFamily = NunitoFontFamily
                    )
                }
                innerTextField()
            }
        }

        if (!readOnly && query.isNotEmpty()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Clear text",
                tint = GreyColor.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(18.dp)
                    .scale(0.8f)
                    .addPressEffect {
                        onQueryChange("")
                    }
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search Icon",
                tint = GreyColor.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
