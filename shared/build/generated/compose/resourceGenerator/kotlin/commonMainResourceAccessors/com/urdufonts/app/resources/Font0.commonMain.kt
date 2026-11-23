@file:OptIn(InternalResourceApi::class)

package com.urdufonts.app.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/com.urdufonts.app.resources/"

@delegate:ResourceContentHash(-180_298_654)
public val Res.font.nunito_bold: FontResource by lazy {
      FontResource("font:nunito_bold", setOf(
        ResourceItem(setOf(), "${MD}font/nunito_bold.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(-1_328_020_861)
public val Res.font.nunito_medium: FontResource by lazy {
      FontResource("font:nunito_medium", setOf(
        ResourceItem(setOf(), "${MD}font/nunito_medium.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(186_981_837)
public val Res.font.nunito_regular: FontResource by lazy {
      FontResource("font:nunito_regular", setOf(
        ResourceItem(setOf(), "${MD}font/nunito_regular.ttf", -1, -1),
      ))
    }

@delegate:ResourceContentHash(2_010_531_770)
public val Res.font.nunito_semi_bold: FontResource by lazy {
      FontResource("font:nunito_semi_bold", setOf(
        ResourceItem(setOf(), "${MD}font/nunito_semi_bold.ttf", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainFont0Resources(map: MutableMap<String, FontResource>) {
  map.put("nunito_bold", Res.font.nunito_bold)
  map.put("nunito_medium", Res.font.nunito_medium)
  map.put("nunito_regular", Res.font.nunito_regular)
  map.put("nunito_semi_bold", Res.font.nunito_semi_bold)
}
