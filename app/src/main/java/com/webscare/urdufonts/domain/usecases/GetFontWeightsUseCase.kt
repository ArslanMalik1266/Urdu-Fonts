package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository
import java.io.File

class GetFontWeightsUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(fontItem: FontItem): Result<List<Pair<String, File>>> {
        return repository.getFontWeightFiles(fontItem)
    }
}