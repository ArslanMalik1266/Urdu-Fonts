package com.urdufonts.app.domain.usecases
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import java.io.File

class DownloadFontToDeviceUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(
        fontItem: FontItem,
        onProgress: (Float) -> Unit
    ): Result<File> {
        return repository.downloadFontToDevice(fontItem, onProgress)
    }
}