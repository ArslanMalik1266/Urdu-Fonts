package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.StyleItem

interface StylesRepository {
    suspend fun getStyles(): Result<List<StyleItem>>
}