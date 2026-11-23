package com.urdufonts.app.ui.support

import com.urdufonts.app.resources.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.urdufonts.app.domain.models.TicketLimits
import com.urdufonts.app.ui.support.kit.AppBottomSheet
import com.urdufonts.app.ui.support.kit.PrimaryButton
import com.urdufonts.app.ui.support.kit.SecondaryButton
import com.urdufonts.app.ui.support.kit.Dimens
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * "Is your problem solved?", in place of the reply bar while support waits for the customer: Yes closes the
 * ticket (then the stars), Not yet asks what is still wrong. When the ticket closes by itself, it says when.
 */
@Composable
internal fun ResolutionCard(autoClosesAt: Long, working: Boolean, onSolved: () -> Unit, onNotSolved: () -> Unit) {
    FeedbackCard(
        title = stringResource(Res.string.chat_resolve_title),
        message = stringResource(Res.string.chat_resolve_message) + if (autoClosesAt > 0) {
            " " + stringResource(Res.string.chat_resolve_auto_close, dateText(localDateTimeOf(autoClosesAt, TimeZone.currentSystemDefault()).date))
        } else {
            ""
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space12), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(text = stringResource(Res.string.chat_resolve_no), onClick = onNotSolved, enabled = !working, modifier = Modifier.weight(1f))
            PrimaryButton(text = stringResource(Res.string.chat_resolve_yes), enabled = true, loading = working, onClick = onSolved, modifier = Modifier.weight(1f))
        }
    }
}

/** On a closed ticket that can still be rated: tapping a star opens the rating sheet on it. */
@Composable
internal fun RatingCard(onPick: (Int) -> Unit) {
    // The stars up to the one tapped fill at once; the sheet follows a moment later
    var picked by remember { mutableIntStateOf(0) }
    LaunchedEffect(picked) {
        if (picked > 0) {
            delay(PICK_DELAY_MILLIS)
            onPick(picked)
            picked = 0
        }
    }
    FeedbackCard(title = stringResource(Res.string.chat_rate_title), message = stringResource(Res.string.chat_rate_message)) {
        StarRow(filled = picked, starSize = Dimens.StarLarge, onPick = { if (picked == 0) picked = it })
    }
}

/** The line at the end of a conversation the customer has rated. */
@Composable
internal fun RatedLine(stars: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.Space8)) {
        Text(text = stringResource(Res.string.chat_rated), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        StarRow(filled = stars, starSize = Dimens.StarSmall, modifier = Modifier.padding(top = Dimens.Space4))
    }
}

/**
 * The panel the resolution question and the rating use: attached to the left, right and bottom edges of the
 * screen like a bottom sheet, with only a thin line along its top (no gaps round it, no border, no shadow).
 */
@Composable
private fun FeedbackCard(title: String, message: String, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface).navigationBarsPadding()) {
        HorizontalDivider(thickness = Dimens.SupportBorder, color = colors.outlineVariant)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.Space16, vertical = Dimens.Space16),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, textAlign = TextAlign.Center)
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Column(Modifier.padding(top = Dimens.Space12)) { content() }
        }
    }
}

/** "What is still wrong?": the optional note that goes back to support with a "not yet". */
@Composable
internal fun NotSolvedSheet(onDismiss: () -> Unit, onSend: (reason: String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    FeedbackSheet(
        title = stringResource(Res.string.chat_reason_title),
        button = stringResource(Res.string.chat_reason_send),
        onDismiss = onDismiss,
        onConfirm = { onSend(reason) },
    ) {
        SupportField(
            value = reason,
            onValueChange = { reason = it.take(TicketLimits.REASON_MAX) },
            hint = stringResource(Res.string.chat_reason_hint),
            singleLine = false,
            minHeight = Dimens.SheetNoteMinHeight,
        )
    }
}

/** The rating sheet: it opens on the star that was tapped (it can still be changed), with room for a comment. */
@Composable
internal fun RatingSheet(initialStars: Int, onDismiss: () -> Unit, onSend: (stars: Int, comment: String) -> Unit) {
    var stars by remember { mutableIntStateOf(initialStars) }
    var comment by remember { mutableStateOf("") }
    FeedbackSheet(
        title = stringResource(Res.string.chat_rate_title),
        button = stringResource(Res.string.chat_rate_submit),
        onDismiss = onDismiss,
        onConfirm = { onSend(stars, comment) },
    ) {
        StarRow(filled = stars, starSize = Dimens.StarLarge, onPick = { stars = it }, modifier = Modifier.align(Alignment.CenterHorizontally))
        SupportField(
            value = comment,
            onValueChange = { comment = it.take(TicketLimits.COMMENT_MAX) },
            hint = stringResource(Res.string.chat_rate_comment_hint),
            singleLine = false,
            minHeight = Dimens.SheetNoteMinHeight,
            modifier = Modifier.padding(top = Dimens.Space16),
        )
    }
}

/** A sheet with a title, its content and one main button; it slides away before the action runs. */
@Composable
private fun FeedbackSheet(
    title: String,
    button: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = Dimens.Space16, end = Dimens.Space16, bottom = Dimens.Space16),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.Space16),
            )
            content()
            PrimaryButton(text = button, enabled = true, onClick = onConfirm, modifier = Modifier.padding(top = Dimens.Space16).fillMaxWidth())
        }
    }
}

/** Five stars, [filled] of them in the brand colour. With [onPick] each star can be tapped to choose a rating. */
@Composable
internal fun StarRow(filled: Int, starSize: Dp, modifier: Modifier = Modifier, onPick: ((Int) -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(starSize / STAR_GAP_SHARE), modifier = modifier) {
        repeat(STAR_COUNT) { index ->
            val stars = index + 1
            val label = stringResource(Res.string.chat_star, stars)
            val pick = onPick?.let { rememberHapticClick({ it(stars) }) }
            Star(
                color = if (stars <= filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .size(starSize)
                    .semantics { contentDescription = label }
                    .then(if (pick != null) Modifier.plainTap(pick) else Modifier),
            )
        }
    }
}

/** A five-pointed star, drawn to fill its box. */
@Composable
private fun Star(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val outer = size.minDimension / 2f
        val path = Path()
        repeat(STAR_POINTS * 2) { corner ->
            val radius = if (corner % 2 == 0) outer else outer * STAR_INNER
            // Starts at the top point and goes round clockwise
            val angle = -PI / 2 + corner * PI / STAR_POINTS
            val x = centre.x + (radius * cos(angle)).toFloat()
            val y = centre.y + (radius * sin(angle)).toFloat()
            if (corner == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color)
    }
}

private const val PICK_DELAY_MILLIS = 350L
private const val STAR_COUNT = 5
private const val STAR_POINTS = 5
private const val STAR_GAP_SHARE = 4

// How far in the star's inner corners sit, as a share of its outer radius
private const val STAR_INNER = 0.42f
