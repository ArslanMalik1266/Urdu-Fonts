package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository
import java.io.File

class GetFontPreviewUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(fontItem: FontItem): Result<File> {
        return repository.getFontFile(fontItem)
    }
}