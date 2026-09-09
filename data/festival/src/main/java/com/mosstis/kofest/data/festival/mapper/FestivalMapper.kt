package com.mosstis.kofest.data.festival.mapper

import com.mosstis.kofest.data.festival.remote.FestivalDto
import com.mosstis.kofest.data.festival.remote.RegionDto
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalState
import com.mosstis.kofest.domain.festival.model.RegionBucket
import java.time.LocalDate

/**
 * 응답 한 건이 깨져 있어도 나머지를 살린다.
 *
 * 배치에서 한 행 때문에 78건이 다 날아갔던 일과 같은 이유다 —
 * 날짜가 이상한 축제 하나 때문에 목록 전체가 비면 안 된다.
 */
internal fun List<FestivalDto>.toDomain(): List<Festival> = mapNotNull { it.toDomainOrNull() }

internal fun FestivalDto.toDomainOrNull(): Festival? {
    val start = startDate.toLocalDateOrNull() ?: return null
    val end = endDate.toLocalDateOrNull() ?: start

    return Festival(
        contentId = contentId,
        title = title,
        region = region.orEmpty(),
        startDate = start,
        endDate = end,
        thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
        imageUrl = imageUrl?.takeIf { it.isNotBlank() } ?: thumbUrl.toOriginalImageUrlOrNull(),
        state = state.toFestivalStateOrUpcoming(),
    )
}

internal fun RegionDto.toDomain(): RegionBucket = RegionBucket(
    code = code,
    name = name,
    count = count,
)

internal fun String.toLocalDateOrNull(): LocalDate? =
    runCatching { LocalDate.parse(this) }.getOrNull()

/**
 * 상태는 서버가 정한다. 모르는 값이 오면 오늘 날짜로 되짚지 않고 UPCOMING 으로 둔다 —
 * 임의로 '종료'를 붙이면 있는 축제가 화면에서 사라진다.
 */
internal fun String?.toFestivalStateOrUpcoming(): FestivalState = when (this) {
    "ongoing" -> FestivalState.ONGOING
    "ended" -> FestivalState.ENDED
    else -> FestivalState.UPCOMING
}
