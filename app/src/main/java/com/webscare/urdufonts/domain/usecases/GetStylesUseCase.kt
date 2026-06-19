package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.domain.repo.StylesRepository

class GetStylesUseCase(
    private val repository: StylesRepository
) {
    suspend operator fun invoke(): Result<List<StyleItem>> {
        return repository.getStyles()
    }
}