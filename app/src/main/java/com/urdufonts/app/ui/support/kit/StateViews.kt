package com.urdufonts.app.ui.support.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.R
import androidx.compose.ui.res.stringResource

/** Centres itself in the space it is given, so always give it height (weight or fillMaxSize). */
@Composable
fun EmptyState(
    icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    /** The little animated scene above the text. */
    art: EmptyArt = EmptyArt.Float,
    /** A smaller version for a slot inside a page (Home's Recently Played): small scene, text and pill. */
    compact: Boolean = false,
) {
    Column(
        modifier = modifier.padding(horizontal = Dimens.Space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) Dimens.Space8 else Dimens.Space12, Alignment.CenterVertically),
    ) {
        EmptyIllustration(icon = icon, art = art, tint = iconTint, scale = if (compact) COMPACT_SCALE else 1f)
        // The title and its message read as one block, close together.
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionText != null && onAction != null) {
            if (compact) {
                PrimaryButton(text = actionText, onClick = onAction, compact = true)
            } else {
                PrimaryButton(text = actionText, onClick = onAction, modifier = Modifier.fillMaxWidth())
            }
        }
        if (secondaryActionText != null && onSecondaryAction != null) {
            SecondaryButton(text = secondaryActionText, onClick = onSecondaryAction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.error_generic),
    onRetry: (() -> Unit)? = null,
) {
    EmptyState(
        icon = R.drawable.ic_sp_warning,
        title = stringResource(R.string.error_title),
        message = message,
        modifier = modifier,
        actionText = onRetry?.let { stringResource(R.string.action_retry) },
        onAction = onRetry,
        iconTint = MaterialTheme.colorScheme.error,
        art = EmptyArt.Alert,
    )
}

/** The compact empty state's scene, as a part of the full one. */
private const val COMPACT_SCALE = 0.6f
