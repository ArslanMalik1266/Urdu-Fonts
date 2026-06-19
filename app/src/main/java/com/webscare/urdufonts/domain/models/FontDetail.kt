package com.webscare.urdufonts.domain.models

data class FontWeightSample(
    val id: String,
    val label: String,      // e.g. "Thin 100", "Extra Light 200"
    val urduText: String,
)

data class FontDetail(
    val id: String,
    val name: String,              // e.g. "Aref Ruqaa"
    val previewText: String,       // large urdu preview text
    val weightsCount: Int,         // e.g. 6
    val category: String,          // e.g. "Ruqaa"
    val fontFamily: String,        // e.g. "Urdu Font Family"
    val weightSamples: List<FontWeightSample> = emptyList(),
    val aboutText: String = "",
)