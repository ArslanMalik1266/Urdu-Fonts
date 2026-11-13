package com.urdufonts.app.ui.support

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.TicketAttachment
import com.urdufonts.app.ui.support.kit.AppIcon
import com.urdufonts.app.ui.support.kit.LoadingSpinner
import com.urdufonts.app.ui.support.kit.SkeletonBox
import com.urdufonts.app.ui.support.kit.ShimmerHost
import com.urdufonts.app.ui.support.kit.addPressEffect
import com.urdufonts.app.ui.support.kit.Dimens
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.urdufonts.app.R

private const val PICTURE_RATIO = 4f / 3f

// A file still on its way is drawn a little faded
private const val SENDING_ALPHA = 0.6f

/** The frame every picture in the conversation sits in. */
@Composable
private fun PictureFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .widthIn(max = Dimens.ChatPictureMaxWidth)
            .fillMaxWidth()
            .aspectRatio(PICTURE_RATIO)
            .clip(shape)
            .border(Dimens.SupportBorder, MaterialTheme.colorScheme.outlineVariant, shape),
    ) { content() }
}

/**
 * A file inside a confirmed message. A picture is shown itself (a skeleton stands in while it downloads); any
 * other file is a card with its name and size. A tap opens it in the phone's own viewer. [sentBytes] is the
 * picture itself when it was sent from this screen.
 */
@Composable
internal fun MessageAttachment(
    attachment: TicketAttachment,
    picturePath: String?,
    opening: Boolean,
    onOpen: () -> Unit,
    sentBytes: ByteArray? = null,
) {
    if (attachment.isImage) {
        val bitmap = sentBytes?.let { rememberBytesBitmap(it) } ?: picturePath?.let { rememberImageFile(it, it, THUMBNAIL_DECODE_SIDE) }
        PictureFrame(modifier = if (picturePath != null) Modifier.addPressEffect(onClick = rememberHapticClick(onOpen)) else Modifier) {
            Picture(bitmap, attachment.fileName)
        }
    } else {
        FileCard(
            icon = R.drawable.ic_sp_file,
            title = attachment.fileName,
            subtitle = fileSizeText(attachment.sizeBytes),
            modifier = Modifier.addPressEffect(onClick = rememberHapticClick(onOpen), enabled = !opening),
        ) {
            Box(Modifier.padding(end = Dimens.Space8)) {
                // The file is being fetched because it was tapped: an action in progress
                if (opening) LoadingSpinner(size = Dimens.SupportLoader) else AppIcon(R.drawable.ic_sp_download, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = Dimens.SupportIcon)
            }
        }
    }
}

/** The file of a message that is still being sent: the picture straight from memory, or the file's card. */
@Composable
internal fun OutgoingAttachment(draft: AttachmentDraft) {
    if (draft.isImage) {
        PictureFrame(modifier = Modifier.alpha(SENDING_ALPHA)) { Picture(rememberBytesBitmap(draft.bytes), draft.fileName) }
    } else {
        FileCard(icon = R.drawable.ic_sp_file, title = draft.fileName, subtitle = fileSizeText(draft.sizeBytes), modifier = Modifier.alpha(SENDING_ALPHA))
    }
}

@Composable
private fun Picture(bitmap: ImageBitmap?, description: String) {
    if (bitmap == null) {
        ShimmerHost { SkeletonBox(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.medium) }
    } else {
        Image(bitmap = bitmap, contentDescription = description, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

/** A picture held in memory (sent from this phone), decoded once. */
@Composable
private fun rememberBytesBitmap(bytes: ByteArray): ImageBitmap? = remember(bytes) { runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap() }.getOrNull() }
