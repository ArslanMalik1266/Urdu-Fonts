package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.domain.models.FontDetail
import com.webscare.urdufonts.domain.models.FontWeightSample
import com.webscare.urdufonts.domain.repo.FontDetailRepository
import kotlinx.coroutines.delay

class FontDetailRepositoryImpl : FontDetailRepository {

    override suspend fun getFontDetail(fontId: String): Result<FontDetail> {
        return try {
            // Simulate network latency
            delay(500)

            val dummyDetail = FontDetail(
                id = fontId,
                name = "Aref Ruqaa",
                previewText = "بہتر کل کی امید اور کامل یقین",
                weightsCount = 6,
                category = "Ruqaa",
                fontFamily = "Urdu Font Family",
                weightSamples = listOf(
                    FontWeightSample(
                        id = "thin_100",
                        label = "Thin 100",
                        urduText = "بہتر کل کی امید اور کامل یقین"
                    ),
                    FontWeightSample(
                        id = "extra_light_200",
                        label = "Extra Light 200",
                        urduText = "بہتر کل کی امید اور کامل یقین"
                    ),
                ),
                aboutText = "Aref Ruqaa is an elegant Arabic font family designed by " +
                        "Abdullah Aref, Khaled Hosny, and Hermann Zapf. It is a trademark " +
                        "of American Mathematical Society and was released in 2016. It " +
                        "aspires to capture the essence of the classic calligraphic style. " +
                        "Compatible with various Unicode blocks, it has a character set of " +
                        "305 comprising letters and symbols with a glyph count of 264. Its " +
                        "supporting languages include Arabic and Latin with partial support " +
                        "for Urdu, Persian, Greek, and more."
            )

            Result.success(dummyDetail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}