package com.urdufonts.app.ui.support.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.urdufonts.app.ui.support.kit.Dimens

@Composable
fun AppChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    idleColor: Color = MaterialTheme.colorScheme.surface,
    icon: Int? = null,
    horizontalPadding: Dp = Dimens.Space12,
) {
    val (container, content) = smallChoiceColors(selected, idleColor)
    Box(
        modifier = modifier
            .addPressEffect(onClick = onClick, role = Role.Tab)
            .height(Dimens.ChipHeight)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(container)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space4 * 1.5f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) AppIcon(icon = icon, contentDescription = null, tint = content, size = Dimens.ChipIcon)
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1)
        }
    }
}


/** The two colours of a small choice: its fill and its text. */
data class ChoiceColors(val container: Color, val content: Color)

private const val SMALL_SOFTEN = 0.08f
private const val SELECTION_FADE_MS = 180

/** A small choice (a chip): the accent, a touch lighter, when chosen; [idle] when not. */
@Composable
fun smallChoiceColors(selected: Boolean, idle: Color = MaterialTheme.colorScheme.surface): ChoiceColors {
    val colors = MaterialTheme.colorScheme
    val container by androidx.compose.animation.animateColorAsState(
        if (selected) androidx.compose.ui.graphics.lerp(colors.primary, colors.surface, SMALL_SOFTEN) else idle,
        androidx.compose.animation.core.tween(SELECTION_FADE_MS), label = "choiceFill"
    )
    val content by androidx.compose.animation.animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurface,
        androidx.compose.animation.core.tween(SELECTION_FADE_MS), label = "choiceInk"
    )
    return ChoiceColors(container, content)
}
