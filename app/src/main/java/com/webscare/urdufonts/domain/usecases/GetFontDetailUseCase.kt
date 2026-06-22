package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository

class GetFontDetailUseCase(
    private val repository: FontRepository
) {
    suspend operator fun invoke(fontId: String): Result<FontItem> {
        val font = repository.getFontById(fontId)
        return if (font != null) {
            Result.success(font)
        } else {
            Result.failure(NoSuchElementException("Font with id $fontId not found"))
        }
    }
}