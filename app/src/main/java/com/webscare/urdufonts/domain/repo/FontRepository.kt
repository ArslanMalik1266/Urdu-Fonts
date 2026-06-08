package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.FontItem

interface FontRepository {
    suspend fun getFonts(): List<FontItem>
}