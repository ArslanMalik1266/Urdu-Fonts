package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.repo.FontRepository

class GetFontsUseCase(
    private val repository: FontRepository
) {
    suspend operator fun invoke() = repository.getFonts()
}