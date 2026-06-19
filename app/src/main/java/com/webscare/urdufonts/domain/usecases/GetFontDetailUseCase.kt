package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.FontDetail
import com.webscare.urdufonts.domain.repo.FontDetailRepository

class GetFontDetailUseCase(
    private val repository: FontDetailRepository
) {
    suspend operator fun invoke(fontId: String): Result<FontDetail> {
        return repository.getFontDetail(fontId)
    }
}