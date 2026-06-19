package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.CategoryItem

interface CategoriesRepository {
    suspend fun getCategories(): Result<List<CategoryItem>>
}
