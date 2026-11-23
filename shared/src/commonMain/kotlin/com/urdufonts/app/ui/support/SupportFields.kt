package com.urdufonts.app.ui.support

import org.jetbrains.compose.resources.DrawableResource
import com.urdufonts.app.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.ui.support.kit.AppIcon
import com.urdufonts.app.ui.support.kit.addPressEffect
import com.urdufonts.app.ui.support.kit.Dimens
import org.jetbrains.compose.resources.stringResource

/** The small label over a form field, with an optional "12/255" counter at the end of the row. */
@Composable
internal fun FieldLabel(text: String, counter: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.Space4)) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (counter != null) Text(text = counter, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The line under a field that says what is wrong with it. */
@Composable
internal fun FieldError(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = Dimens.Space4))
}

/**
 * The text field of the support screens: a white rounded box whose border turns blue while it is being typed
 * in (like the search field) and red while its content is refused. [minHeight] makes it a multi-line box.
 */
@Composable
internal fun SupportField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    minHeight: Dp = Dimens.ButtonHeight,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    val colors = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    val border by animateColorAsState(
        when {
            error != null -> colors.error
            focused -> colors.primary
            else -> colors.outlineVariant
        },
        label = "fieldBorder",
    )
    val textStyle = MaterialTheme.typography.bodyMedium
    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization,
                imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
            ),
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
            decorationBox = { field ->
                Box(
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.surface)
                        .border(Dimens.SupportBorder, border, MaterialTheme.shapes.medium)
                        .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
                ) {
                    if (value.isEmpty()) Text(text = hint, style = textStyle, color = colors.onSurfaceVariant)
                    field()
                }
            },
        )
        if (error != null) FieldError(error)
    }
}

/**
 * Where a file is added to a new ticket. Empty, a tap opens Gallery / Files above it; with a file, it shows
 * the file and a way to take it off again. [problem] says why the last pick was refused.
 */
@Composable
internal fun AttachmentBox(
    attachment: AttachmentDraft?,
    problem: AttachmentProblem?,
    onGallery: () -> Unit,
    onFiles: () -> Unit,
    onRemove: () -> Unit,
) {
    var choosing by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
        if (attachment == null) {
            FileCard(
                icon = Res.drawable.ic_sp_image,
                title = stringResource(Res.string.ticket_attach_title),
                subtitle = stringResource(Res.string.ticket_attach_note),
                modifier = Modifier.addPressEffect(onClick = rememberHapticClick({ choosing = !choosing })),
            ) { AppIcon(Res.drawable.ic_sp_add, null, tint = MaterialTheme.colorScheme.primary, size = Dimens.SupportIcon) }
            AnimatedVisibility(
                visible = choosing,
                enter = fadeIn() + scaleIn(initialScale = MENU_FROM_SCALE, transformOrigin = TransformOrigin(0.5f, 0f)),
                exit = fadeOut() + scaleOut(targetScale = MENU_FROM_SCALE, transformOrigin = TransformOrigin(0.5f, 0f)),
            ) {
                AttachmentSourceMenu(
                    onGallery = {
                        choosing = false
                        onGallery()
                    },
                    onFiles = {
                        choosing = false
                        onFiles()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            FileCard(
                icon = if (attachment.isImage) Res.drawable.ic_sp_image else Res.drawable.ic_sp_file,
                title = attachment.fileName,
                subtitle = fileSizeText(attachment.sizeBytes),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Dimens.SettingsIconBox).addPressEffect(onClick = rememberHapticClick(onRemove)),
                ) {
                    AppIcon(Res.drawable.ic_sp_close, stringResource(Res.string.ticket_attach_remove), tint = MaterialTheme.colorScheme.onSurfaceVariant, size = Dimens.SupportIcon)
                }
            }
        }
        if (problem != null) FieldError(stringResource(problem.text))
    }
}

/** A file as a card: icon, name, a second line, and something at the end. */
@Composable
internal fun FileCard(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    end: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .border(Dimens.SupportBorder, colors.outlineVariant, MaterialTheme.shapes.medium)
            .padding(start = Dimens.Space12, end = Dimens.Space4, top = Dimens.Space8, bottom = Dimens.Space8),
    ) {
        AppIcon(icon, null, tint = colors.primary)
        Box(Modifier.width(Dimens.Space12))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
        }
        end()
    }
}

/** Gallery and Files as two rows on a white card with a soft border (no shadow). */
@Composable
internal fun AttachmentSourceMenu(onGallery: () -> Unit, onFiles: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .border(Dimens.SupportBorder, colors.outlineVariant, MaterialTheme.shapes.medium)
            .padding(vertical = Dimens.Space4),
    ) {
        MenuRow(Res.drawable.ic_sp_image, stringResource(Res.string.chat_attach_gallery), onGallery)
        MenuRow(Res.drawable.ic_sp_file, stringResource(Res.string.chat_attach_files), onFiles)
    }
}

@Composable
private fun MenuRow(icon: DrawableResource, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        modifier = Modifier
            .addPressEffect(onClick = rememberHapticClick(onClick))
            .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
    ) {
        AppIcon(icon, null, tint = MaterialTheme.colorScheme.primary, size = Dimens.SupportIcon)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private const val MENU_FROM_SCALE = 0.9f
