package com.urdufonts.app.ui.util

/** The readable text of an HTML snippet from the API (font descriptions). */
expect fun htmlToPlainText(html: String): String

/** Used by iOS: line breaks for block tags, tags removed, common entities decoded. */
internal fun stripHtml(html: String): String {
    val withBreaks = html
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</(p|div|li|h[1-6])>"), "\n")
    val noTags = withBreaks.replace(Regex("<[^>]+>"), "")
    val decoded = noTags
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("&#(\\d+);")) { m -> m.groupValues[1].toIntOrNull()?.let { code -> codeToString(code) } ?: m.value }
        .replace("&amp;", "&")
    return decoded.replace(Regex("\n{3,}"), "\n\n").trim()
}

private fun codeToString(code: Int): String =
    if (code in 0..0xFFFF) code.toChar().toString()
    else {
        val v = code - 0x10000
        charArrayOf((0xD800 + (v shr 10)).toChar(), (0xDC00 + (v and 0x3FF)).toChar()).concatToString()
    }
