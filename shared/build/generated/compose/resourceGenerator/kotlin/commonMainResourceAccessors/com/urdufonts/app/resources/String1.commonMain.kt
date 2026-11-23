@file:OptIn(InternalResourceApi::class)

package com.urdufonts.app.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem
import org.jetbrains.compose.resources.StringResource

private const val MD: String = "composeResources/com.urdufonts.app.resources/"

@delegate:ResourceContentHash(1_432_781_160)
public val Res.string.ticket_error_subject: StringResource by lazy {
      StringResource("string:ticket_error_subject", "ticket_error_subject", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 4917, 56),
      ))
    }

@delegate:ResourceContentHash(-1_082_588_431)
public val Res.string.ticket_new_title: StringResource by lazy {
      StringResource("string:ticket_new_title", "ticket_new_title", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 4974, 48),
      ))
    }

@delegate:ResourceContentHash(110_829_751)
public val Res.string.ticket_subject_hint: StringResource by lazy {
      StringResource("string:ticket_subject_hint", "ticket_subject_hint", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 5023, 47),
      ))
    }

@delegate:ResourceContentHash(-1_821_951_602)
public val Res.string.ticket_subject_label: StringResource by lazy {
      StringResource("string:ticket_subject_label", "ticket_subject_label", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 5071, 40),
      ))
    }

@delegate:ResourceContentHash(-264_924_894)
public val Res.string.ticket_submit: StringResource by lazy {
      StringResource("string:ticket_submit", "ticket_submit", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 5112, 41),
      ))
    }

@delegate:ResourceContentHash(-1_317_420_605)
public val Res.string.ticket_topic_label: StringResource by lazy {
      StringResource("string:ticket_topic_label", "ticket_topic_label", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 5154, 50),
      ))
    }

@delegate:ResourceContentHash(-2_125_310_226)
public val Res.string.time_ago_just_now: StringResource by lazy {
      StringResource("string:time_ago_just_now", "time_ago_just_now", setOf(
        ResourceItem(setOf(), "${MD}values/strings_support.commonMain.cvr", 5205, 37),
      ))
    }

@delegate:ResourceContentHash(-1_093_257_026)
public val Res.string.urdu_canvas_app_name: StringResource by lazy {
      StringResource("string:urdu_canvas_app_name", "urdu_canvas_app_name", setOf(
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1234, 44),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString1Resources(map: MutableMap<String, StringResource>) {
  map.put("ticket_error_subject", Res.string.ticket_error_subject)
  map.put("ticket_new_title", Res.string.ticket_new_title)
  map.put("ticket_subject_hint", Res.string.ticket_subject_hint)
  map.put("ticket_subject_label", Res.string.ticket_subject_label)
  map.put("ticket_submit", Res.string.ticket_submit)
  map.put("ticket_topic_label", Res.string.ticket_topic_label)
  map.put("time_ago_just_now", Res.string.time_ago_just_now)
  map.put("urdu_canvas_app_name", Res.string.urdu_canvas_app_name)
}
