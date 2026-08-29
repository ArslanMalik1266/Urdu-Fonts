package com.urdufonts.app.data.mapper

import com.urdufonts.app.data.remote.dto.MoreAppDto
import com.urdufonts.app.domain.models.MoreAppItem

fun MoreAppDto.toDomain(): MoreAppItem {
    return MoreAppItem(
        id = id,
        name = name,
        iconUrl = icon,
        playstoreUrl = playstoreLink
    )
}
