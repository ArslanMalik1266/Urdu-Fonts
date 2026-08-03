package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.domain.repo.CategoriesRepository

class GetCategoriesUseCase(
    private val repository: CategoriesRepository
) {
    suspend operator fun invoke(): Result<List<CategoryItem>> = repository.getCategories()
}