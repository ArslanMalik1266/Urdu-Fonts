package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.domain.repo.CategoriesRepository
import kotlinx.coroutines.delay

class CategoriesRepositoryImpl : CategoriesRepository {

    override suspend fun getCategories(): Result<List<CategoryItem>> {
        return try {
            // Simulate network latency
            delay(500)

            val dummyCategories = listOf(
                CategoryItem(id = "1", title = "Bold", urduText = "زبان اردو"),
                CategoryItem(id = "2", title = "Handwriting", urduText = "زبان اردو"),
                CategoryItem(id = "3", title = "Kufic", urduText = "زبان اردو"),
                CategoryItem(id = "4", title = "Nastaliq", urduText = "زبان اردو"),
                CategoryItem(id = "5", title = "Rounded", urduText = "زبان اردو"),
                CategoryItem(id = "6", title = "Ruqaa", urduText = "زبان اردو"),
                CategoryItem(id = "7", title = "Thin", urduText = "زبان اردو"),
                CategoryItem(id = "8", title = "Thuluth", urduText = "زبان اردو"),
                CategoryItem(id = "9", title = "Bold", urduText = "زبان اردو"),
                CategoryItem(id = "11", title = "Handwriting", urduText = "زبان اردو"),
                CategoryItem(id = "12", title = "Kufic", urduText = "زبان اردو"),
                CategoryItem(id = "13", title = "Nastaliq", urduText = "زبان اردو"),
                CategoryItem(id = "14", title = "Rounded", urduText = "زبان اردو"),
                CategoryItem(id = "15", title = "Ruqaa", urduText = "زبان اردو"),
                CategoryItem(id = "16", title = "Thin", urduText = "زبان اردو"),
                CategoryItem(id = "17", title = "Thuluth", urduText = "زبان اردو"),
                CategoryItem(id = "18", title = "Bold", urduText = "زبان اردو"),
                CategoryItem(id = "19", title = "Handwriting", urduText = "زبان اردو"),
                CategoryItem(id = "20", title = "Kufic", urduText = "زبان اردو"),
                CategoryItem(id = "21", title = "Nastaliq", urduText = "زبان اردو"),
                CategoryItem(id = "22", title = "Rounded", urduText = "زبان اردو"),
                CategoryItem(id = "23", title = "Ruqaa", urduText = "زبان اردو"),
                CategoryItem(id = "24", title = "Thin", urduText = "زبان اردو"),
                CategoryItem(id = "25", title = "Thuluth", urduText = "زبان اردو"),
            )

            Result.success(dummyCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}