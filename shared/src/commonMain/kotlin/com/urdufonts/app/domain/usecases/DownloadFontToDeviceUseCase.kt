package com.urdufonts.app.domain.usecases
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.platform.PlatformFile

class DownloadFontToDeviceUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(
        fontItem: FontItem,
        onProgress: (Float) -> Unit
    ): Result<PlatformFile> {
        return repository.downloadFontToDevice(fontItem, onProgress)
    }
}