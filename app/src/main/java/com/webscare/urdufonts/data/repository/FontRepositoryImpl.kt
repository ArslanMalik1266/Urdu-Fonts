package com.webscare.urdufonts.data.repository

import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.repo.FontRepository

class FontRepositoryImpl : FontRepository {

    override suspend fun getFonts(): List<FontItem> {
        return listOf(
            FontItem(id = "1", name = "Aref Ruqaa", style = "Naskh", category = "Rounded", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "2", name = "Amiri", style = "Naskh", category = "Serif", weightCount = 4, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "3", name = "Scheherazade", style = "Nastaliq", category = "Traditional", weightCount = 2, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "4", name = "Noto Nastaliq Urdu", style = "Nastaliq", category = "Modern", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "5", name = "Jameel Noori Nastaleeq", style = "Nastaliq", category = "Traditional", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "6", name = "Faiz Lahori Nastaleeq", style = "Nastaliq", category = "Calligraphy", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "7", name = "Gulzar", style = "Naskh", category = "Decorative", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "8", name = "Mehr Nastaleeq", style = "Nastaliq", category = "Calligraphy", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "9", name = "Urdu Typesetting", style = "Naskh", category = "Rounded", weightCount = 2, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "10", name = "Al Qalam Quran", style = "Naskh", category = "Serif", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "11", name = "Pak Nastaleeq", style = "Nastaliq", category = "Traditional", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "12", name = "Jameel Khushkhati", style = "Nastaliq", category = "Calligraphy", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "13", name = "Nafees Web Naskh", style = "Naskh", category = "Modern", weightCount = 2, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "14", name = "Alvi Nastaleeq", style = "Nastaliq", category = "Traditional", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "15", name = "Tehreer Urdu", style = "Naskh", category = "Rounded", weightCount = 2, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "16", name = "Decotype Naskh", style = "Naskh", category = "Serif", weightCount = 3, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "17", name = "Naseem Urdu", style = "Nastaliq", category = "Calligraphy", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "18", name = "Khat-e-Sulas", style = "Sulus", category = "Decorative", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "19", name = "Nafees Pakistani Naskh", style = "Naskh", category = "Modern", weightCount = 2, previewImage = R.drawable.item_view_image_temperary),
            FontItem(id = "20", name = "Lateef", style = "Naskh", category = "Rounded", weightCount = 1, previewImage = R.drawable.item_view_image_temperary),
        )
    }
}