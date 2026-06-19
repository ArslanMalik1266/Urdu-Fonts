package com.webscare.urdufonts.domain.repo

import com.webscare.urdufonts.domain.models.FontDetail

interface FontDetailRepository {
    suspend fun getFontDetail(fontId: String): Result<FontDetail>
}