package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.platform.PlatformFile

class GetFontPreviewUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(fontItem: FontItem): Result<PlatformFile> {
        return repository.getFontFile(fontItem)
    }
}