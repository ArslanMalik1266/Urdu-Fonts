package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.domain.repo.StylesRepository
import kotlinx.coroutines.delay

class StylesRepositoryImpl : StylesRepository {

    override suspend fun getStyles(): Result<List<StyleItem>> {
        return try {
            // Simulate network latency
            delay(500)

            val dummyStyles = listOf(
                StyleItem(id = "1", title = "Calligraphic", urduText = "اردو"),
                StyleItem(id = "2", title = "Circular", urduText = "اردو"),
                StyleItem(id = "3", title = "Digital", urduText = "اردو"),
                StyleItem(id = "4", title = "Dotted", urduText = "اردو"),
                StyleItem(id = "5", title = "Modern", urduText = "اردو"),
                StyleItem(id = "6", title = "Calligraphic", urduText = "اردو"),
                StyleItem(id = "7", title = "Circular", urduText = "اردو"),
                StyleItem(id = "8", title = "Digital", urduText = "اردو"),
                StyleItem(id = "9", title = "Dotted", urduText = "اردو"),
                StyleItem(id = "10", title = "Modern", urduText = "اردو"),
                StyleItem(id = "11", title = "Calligraphic", urduText = "اردو"),
                StyleItem(id = "12", title = "Circular", urduText = "اردو"),
                StyleItem(id = "13", title = "Digital", urduText = "اردو"),
                StyleItem(id = "14", title = "Dotted", urduText = "اردو"),
                StyleItem(id = "15", title = "Modern", urduText = "اردو"),
            )

            Result.success(dummyStyles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}