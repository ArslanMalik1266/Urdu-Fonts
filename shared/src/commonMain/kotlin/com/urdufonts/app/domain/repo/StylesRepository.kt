package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.StyleItem

interface StylesRepository {
    suspend fun getStyles(): Result<List<StyleItem>>
}