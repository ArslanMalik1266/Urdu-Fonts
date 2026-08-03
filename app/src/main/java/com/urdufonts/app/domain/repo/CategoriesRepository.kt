package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.CategoryItem

interface CategoriesRepository {
    suspend fun getCategories(): Result<List<CategoryItem>>
}