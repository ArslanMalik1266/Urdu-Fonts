package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository
import java.io.File

class GetFontPreviewUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(fontItem: FontItem): Result<File> {
        return repository.getFontFile(fontItem)
    }
}