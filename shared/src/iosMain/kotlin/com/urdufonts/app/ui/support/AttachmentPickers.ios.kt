package com.urdufonts.app.ui.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.TicketLimits
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.decodeToImageBitmap
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

// A file a little over the limit is still read, so the customer is told it is too large by the same check as
// everywhere else; anything far bigger is not loaded into memory at all
private const val READ_LIMIT_BYTES = TicketLimits.ATTACHMENT_MAX_BYTES * 2

/** The screen currently on top, to present pickers and viewers from. */
private fun topViewController(): UIViewController? {
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    return generateSequence(root) { it.presentedViewController }.last()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}

private fun mimeTypeOf(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "")
    return UTType.typeWithFilenameExtension(ext)?.preferredMIMEType ?: "application/octet-stream"
}

private fun draftFrom(url: NSURL): AttachmentDraft? {
    val scoped = url.startAccessingSecurityScopedResource()
    try {
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        val name = url.lastPathComponent ?: "attachment"
        val size = data.length.toLong()
        return if (size > READ_LIMIT_BYTES) AttachmentDraft(name, mimeTypeOf(name), ByteArray(0), sizeBytes = size)
        else AttachmentDraft(name, mimeTypeOf(name), data.toByteArray())
    } finally {
        if (scoped) url.stopAccessingSecurityScopedResource()
    }
}

/** Delegates must outlive the picker, so they are kept here while one is open. */
private val openDelegates = mutableSetOf<NSObject>()

private class GalleryDelegate(
    private val onPicked: (AttachmentDraft) -> Unit,
    private val onFailed: () -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        openDelegates.remove(this)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return // closed without choosing
        val provider = result.itemProvider
        val name = (provider.suggestedName ?: "photo") + ".jpg"
        provider.loadDataRepresentationForTypeIdentifier(UTTypeImage.identifier) { data, _ ->
            dispatch_async(dispatch_get_main_queue()) {
                if (data == null) onFailed()
                else if (data.length.toLong() > READ_LIMIT_BYTES) onPicked(AttachmentDraft(name, "image/jpeg", ByteArray(0), sizeBytes = data.length.toLong()))
                else onPicked(AttachmentDraft(name, "image/jpeg", data.toByteArray()))
            }
        }
    }
}

private class FilesDelegate(
    private val onPicked: (AttachmentDraft) -> Unit,
    private val onFailed: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        openDelegates.remove(this)
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val draft = draftFrom(url)
        if (draft != null) onPicked(draft) else onFailed()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        openDelegates.remove(this)
    }
}

@Composable
actual fun rememberAttachmentPickers(onPicked: (AttachmentDraft) -> Unit, onFailed: () -> Unit): AttachmentPickers {
    val picked by rememberUpdatedState(onPicked)
    val failed by rememberUpdatedState(onFailed)
    return remember {
        AttachmentPickers(
            gallery = {
                com.urdufonts.app.ads.suppressNextReturnAppOpen()
                val config = PHPickerConfiguration().apply {
                    filter = PHPickerFilter.imagesFilter
                    selectionLimit = 1
                }
                val delegate = GalleryDelegate({ picked(it) }, { failed() })
                openDelegates += delegate
                val picker = PHPickerViewController(configuration = config).apply { this.delegate = delegate }
                topViewController()?.presentViewController(picker, animated = true, completion = null)
            },
            files = {
                com.urdufonts.app.ads.suppressNextReturnAppOpen()
                val types = listOfNotNull(UTTypeImage, UTTypePDF, UTTypePlainText, UTTypeJSON)
                val delegate = FilesDelegate({ picked(it) }, { failed() })
                openDelegates += delegate
                val picker = UIDocumentPickerViewController(forOpeningContentTypes = types, asCopy = true).apply {
                    this.delegate = delegate
                    allowsMultipleSelection = false
                }
                topViewController()?.presentViewController(picker, animated = true, completion = null)
            },
        )
    }
}

private class PreviewDelegate : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
    override fun documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController): UIViewController =
        topViewController() ?: UIViewController()

    override fun documentInteractionControllerDidEndPreview(controller: UIDocumentInteractionController) {
        openDelegates.remove(this)
    }
}

/** Kept while a preview is open: the controller is not retained by UIKit. */
private var openPreview: UIDocumentInteractionController? = null

@Composable
actual fun rememberCachedFileOpener(): (path: String, mimeType: String) -> Boolean = remember {
    { path, _ ->
        com.urdufonts.app.ads.suppressNextReturnAppOpen()
        val delegate = PreviewDelegate()
        openDelegates += delegate
        val controller = UIDocumentInteractionController.interactionControllerWithURL(NSURL.fileURLWithPath(path))
        controller.delegate = delegate
        openPreview = controller
        controller.presentPreviewAnimated(true)
    }
}

/** Decoded pictures, by path and size, so scrolling back to one does not decode it again. */
private val imageCache = object {
    private val map = LinkedHashMap<String, ImageBitmap>()
    fun get(key: String): ImageBitmap? = map.remove(key)?.also { map[key] = it }
    fun put(key: String, value: ImageBitmap) {
        map[key] = value
        while (map.size > 24) map.remove(map.keys.first())
    }
}

@Composable
actual fun rememberImageFile(path: String, reloadKey: Any, maxSide: Int): ImageBitmap? {
    val key = "$path#$maxSide"
    var bitmap by remember(reloadKey, maxSide) { mutableStateOf(imageCache.get(key)) }
    LaunchedEffect(reloadKey, maxSide) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = SystemFileSystem.source(Path(path)).buffered().use { it.readByteArray() }
                bytes.decodeToImageBitmap().also { imageCache.put(key, it) }
            }.getOrNull()
        }
    }
    return bitmap
}
