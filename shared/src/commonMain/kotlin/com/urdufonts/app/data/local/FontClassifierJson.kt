package com.urdufonts.app.data.local

import com.urdufonts.app.data.remote.AppJson
import com.urdufonts.app.domain.models.FontClassifier
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * How a font's categories and styles are stored in its Room row. The field names match what Gson wrote
 * before, so rows cached by older app versions still read back.
 */
@Serializable
private data class StoredClassifier(
    val id: Int,
    val title: String = "",
    val slug: String = "",
    val description: String? = null,
    val thumbnailUrl: String? = null
)

object FontClassifierJson {
    fun encode(items: List<FontClassifier>?): String = AppJson.encodeToString(
        items.orEmpty().map { StoredClassifier(it.id, it.title, it.slug, it.description, it.thumbnailUrl) }
    )

    fun decode(json: String?): List<FontClassifier> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { AppJson.decodeFromString<List<StoredClassifier>>(json) }.getOrDefault(emptyList())
            .map { FontClassifier(it.id, it.title, it.slug, it.description, it.thumbnailUrl) }
    }
}
