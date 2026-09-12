package com.mosstis.kofest.data.festival.mapper

import com.mosstis.kofest.data.festival.remote.CountDto
import com.mosstis.kofest.data.festival.remote.CountsDto
import com.mosstis.kofest.domain.festival.model.CountBreakdown
import com.mosstis.kofest.domain.festival.model.LanguageCounts

internal fun CountsDto.toDomain(): LanguageCounts =
    LanguageCounts(ko = ko.toDomain(), en = en.toDomain())

internal fun CountDto.toDomain(): CountBreakdown =
    CountBreakdown(festival = festival, place = place, total = total)
