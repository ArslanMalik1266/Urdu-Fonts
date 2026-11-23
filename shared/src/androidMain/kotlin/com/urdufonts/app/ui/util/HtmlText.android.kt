package com.urdufonts.app.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml

actual fun htmlToPlainText(html: String): String = AnnotatedString.fromHtml(html).text
