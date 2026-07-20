package com.webscare.urdufonts.domain.usecases
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository
import java.io.File

class DownloadFontToDeviceUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(
        fontItem: FontItem,
        onProgress: (Float) -> Unit
    ): Result<File> {
        return repository.downloadFontToDevice(fontItem, onProgress)
    }
}