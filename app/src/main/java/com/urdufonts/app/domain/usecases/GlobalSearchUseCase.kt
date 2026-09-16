package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.GlobalSearchResult
import com.urdufonts.app.domain.repo.CategoriesRepository
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.domain.repo.StylesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GlobalSearchUseCase(
    private val fontRepository: FontRepository,
    private val categoriesRepository: CategoriesRepository,
    private val stylesRepository: StylesRepository
) {
    suspend operator fun invoke(query: String): Result<GlobalSearchResult> = coroutineScope {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return@coroutineScope Result.success(GlobalSearchResult())
        }

        try {
            val fontsDeferred = async { fontRepository.getFonts() }
            val categoriesDeferred = async { categoriesRepository.getCategories() }
            val stylesDeferred = async { stylesRepository.getStyles() }

            val allFonts = fontsDeferred.await()
            val categoriesResult = categoriesDeferred.await()
            val stylesResult = stylesDeferred.await()

            val allCategories = categoriesResult.getOrDefault(emptyList())
            val allStyles = stylesResult.getOrDefault(emptyList())

            val matchedFonts = allFonts.filter { font ->
                font.name.contains(trimmedQuery, ignoreCase = true) ||
                font.fontFamily.contains(trimmedQuery, ignoreCase = true) ||
                font.developer.contains(trimmedQuery, ignoreCase = true) ||
                font.tags.any { it.contains(trimmedQuery, ignoreCase = true) }
            }

            val matchedCategories = allCategories.filter { cat ->
                cat.title.contains(trimmedQuery, ignoreCase = true) ||
                cat.slug.contains(trimmedQuery, ignoreCase = true)
            }

            val matchedStyles = allStyles.filter { style ->
                style.title.contains(trimmedQuery, ignoreCase = true) ||
                style.slug.contains(trimmedQuery, ignoreCase = true)
            }

            Result.success(
                GlobalSearchResult(
                    fonts = matchedFonts,
                    categories = matchedCategories,
                    styles = matchedStyles
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
