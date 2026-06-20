package com.webscare.urdufonts.domain.usecases

import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.domain.repo.CategoriesRepository

class GetCategoriesUseCase(
    private val repository: CategoriesRepository
) {
    suspend operator fun invoke(): Result<List<CategoryItem>> = repository.getCategories()
}