package com.urdufonts.app.ui.support

import java.io.File
import androidx.core.content.FileProvider
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.TicketLimits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FILE_TYPES = arrayOf("image/*", "application/pdf", "text/plain", "application/json")

// A file a little over the limit is still read, so the customer is told it is too large by the same check as
// everywhere else; anything far bigger is not loaded into memory at all
private const val READ_LIMIT_BYTES = TicketLimits.ATTACHMENT_MAX_BYTES * 2

@Composable
actual fun rememberAttachmentPickers(onPicked: (AttachmentDraft) -> Unit, onFailed: () -> Unit): AttachmentPickers {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picked = rememberUpdatedState(onPicked)
    val failed = rememberUpdatedState(onFailed)
    val handle: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            scope.launch {
                val draft = withContext(Dispatchers.IO) { read(context, uri) }
                if (draft != null) picked.value(draft) else failed.value()
            }
        }
    }
    // Android's own photo picker: it shows only pictures and needs no storage permission
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), handle)
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), handle)
    return remember(gallery, files) {
        AttachmentPickers(
            gallery = { com.urdufonts.app.ads.suppressNextReturnAppOpen(); gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            files = { com.urdufonts.app.ads.suppressNextReturnAppOpen(); files.launch(FILE_TYPES) },
        )
    }
}

private fun read(context: Context, uri: Uri): AttachmentDraft? = try {
    val resolver = context.contentResolver
    var name = "attachment"
    var size = 0L
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(0)?.let { name = it }
            size = cursor.getLong(1)
        }
    }
    val mimeType = resolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
    if (size > READ_LIMIT_BYTES) {
        // Too big to send anyway: its name and size are enough to say so
        AttachmentDraft(name, mimeType, ByteArray(0), sizeBytes = size)
    } else {
        resolver.openInputStream(uri)?.use { AttachmentDraft(name, mimeType, it.readBytes()) }
    }
} catch (e: Exception) {
    null
}

@Composable
actual fun rememberCachedFileOpener(): (path: String, mimeType: String) -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        { path, mimeType ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeType.ifBlank { "*/*" })
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                com.urdufonts.app.ads.suppressNextReturnAppOpen()
                context.startActivity(intent)
                true
            } catch (e: ActivityNotFoundException) {
                false
            }
        }
    }
}
