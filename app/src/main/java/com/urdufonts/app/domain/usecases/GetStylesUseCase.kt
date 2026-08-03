package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.StyleItem
import com.urdufonts.app.domain.repo.StylesRepository

class GetStylesUseCase(
    private val repository: StylesRepository
) {
    suspend operator fun invoke(): Result<List<StyleItem>> {
        return repository.getStyles()
    }
}