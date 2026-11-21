package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.repo.FontRepository

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