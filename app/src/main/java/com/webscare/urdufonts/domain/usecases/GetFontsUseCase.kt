package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.repo.FontRepository

class GetFontsUseCase(
    private val repository: FontRepository
) {
    suspend operator fun invoke() = repository.getFonts()
}