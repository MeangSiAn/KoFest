package com.mosstis.kofest.data.festival.mapper

import com.mosstis.kofest.data.festival.remote.PlaceDto
import com.mosstis.kofest.data.festival.remote.PlanDayDto
import com.mosstis.kofest.data.festival.remote.PlanMoveDto
import com.mosstis.kofest.data.festival.remote.PlanResponse
import com.mosstis.kofest.data.festival.remote.PlanStopDto
import com.mosstis.kofest.data.festival.remote.StoryBlockDto
import com.mosstis.kofest.data.festival.remote.StoryDto
import com.mosstis.kofest.data.festival.remote.ThemeDto
import com.mosstis.kofest.data.festival.remote.ThemeItemDto
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.domain.festival.model.StoryBlock
import com.mosstis.kofest.domain.festival.model.Theme
import com.mosstis.kofest.domain.festival.model.TravelPlan
import java.time.Instant
import java.time.LocalDate

internal fun PlaceDto.toDomain(): Place = Place(
    contentId = contentId,
    title = title,
    type = type,
    typeName = typeName?.takeIf { it.isNotBlank() },
    region = region?.takeIf { it.isNotBlank() },
    district = district?.takeIf { it.isNotBlank() },
    address = addr?.takeIf { it.isNotBlank() },
    tel = tel?.takeIf { it.isNotBlank() },
    thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    latitude = lat,
    longitude = lng,
)

internal fun ThemeDto.toDomain(baseUrl: String): Theme = Theme(
    id = id,
    badge = badge?.takeIf { it.isNotBlank() },
    title = title,
    subtitle = subtitle?.takeIf { it.isNotBlank() },
    coverUrl = coverUrl?.let { absoluteUrl(it, baseUrl) },
    count = count,
    // 모르는 kind 는 버린다. 서버가 종류를 늘려도 옛 앱이 깨지지 않는다.
    items = items.mapNotNull { it.toDomainOrNull() },
)

private fun ThemeItemDto.toDomainOrNull(): Theme.Item? {
    val itemKind = when (kind) {
        "festival" -> Theme.Kind.FESTIVAL
        "place" -> Theme.Kind.PLACE
        else -> return null
    }
    val from = startDate.toLocalDateOrNull()
    val to = endDate.toLocalDateOrNull() ?: from
    return Theme.Item(
        kind = itemKind,
        contentId = contentId,
        title = title,
        region = region?.takeIf { it.isNotBlank() },
        thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
        period = if (from != null && to != null) from..to else null,
    )
}

internal fun StoryDto.toDomain(baseUrl: String): Story = Story(
    id = id,
    slug = slug,
    title = title,
    excerpt = excerpt?.takeIf { it.isNotBlank() },
    coverUrl = coverUrl?.let { absoluteUrl(it, baseUrl) },
    publishedAt = publishedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?: publishedAt?.let { parseOffset(it) },
    pinned = pinned,
)

/** `2026-09-10T14:02:00+09:00` 처럼 오프셋이 붙은 값 */
private fun parseOffset(raw: String): Instant? =
    runCatching { java.time.OffsetDateTime.parse(raw).toInstant() }.getOrNull()

/** **모르는 type 은 건너뛴다.** 나중에 블록이 늘어도 옛 앱이 안 깨진다 */
internal fun StoryBlockDto.toDomainOrNull(): StoryBlock? = when (type) {
    "h2" -> text?.let { StoryBlock.Heading(level = 2, text = it) }
    "h3" -> text?.let { StoryBlock.Heading(level = 3, text = it) }
    "p" -> text?.let { StoryBlock.Paragraph(it) }
    "ul" -> items?.takeIf { it.isNotEmpty() }?.let { StoryBlock.BulletList(it, ordered = false) }
    "ol" -> items?.takeIf { it.isNotEmpty() }?.let { StoryBlock.BulletList(it, ordered = true) }
    "quote" -> text?.let { StoryBlock.Quote(it) }
    "image" -> url?.let { StoryBlock.Image(it, alt?.takeIf(String::isNotBlank)) }
    "hr" -> StoryBlock.Divider
    "festival" -> contentId?.let { id ->
        val from = startDate.toLocalDateOrNull()
        val to = endDate.toLocalDateOrNull() ?: from
        StoryBlock.FestivalCard(
            contentId = id,
            title = title.orEmpty(),
            region = region?.takeIf { it.isNotBlank() },
            thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
            period = if (from != null && to != null) from..to else null,
        )
    }
    "place" -> contentId?.let { id ->
        StoryBlock.PlaceCard(
            contentId = id,
            title = title.orEmpty(),
            region = region?.takeIf { it.isNotBlank() },
            thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
        )
    }
    else -> null
}

internal fun PlanResponse.toDomain(): TravelPlan = TravelPlan(
    areaName = areaName?.takeIf { it.isNotBlank() },
    moodName = moodName?.takeIf { it.isNotBlank() },
    total = total,
    festivalCount = festivalCount,
    thin = thin,
    days = days.mapNotNull { it.toDomainOrNull() },
)

private fun PlanDayDto.toDomainOrNull(): TravelPlan.Day? {
    val day = date.toLocalDateOrNull() ?: return null
    return TravelPlan.Day(
        date = day,
        label = label?.takeIf { it.isNotBlank() },
        stops = stops.mapNotNull { it.toDomainOrNull() },
    )
}

private fun PlanStopDto.toDomainOrNull(): TravelPlan.Stop? {
    val stopKind = when (kind) {
        "festival" -> TravelPlan.Kind.FESTIVAL
        "place" -> TravelPlan.Kind.PLACE
        "meal" -> TravelPlan.Kind.MEAL
        else -> return null
    }
    return TravelPlan.Stop(
        kind = stopKind,
        time = time?.takeIf { it.isNotBlank() },
        // meal 에는 contentId 가 없다 — 누를 수 없는 자리다
        contentId = contentId?.takeIf { stopKind != TravelPlan.Kind.MEAL },
        title = (title ?: label).orEmpty(),
        region = (region ?: where)?.takeIf { it.isNotBlank() },
        stayMinutes = stay,
        thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
        move = move?.toDomainOrNull(),
    )
}

private fun PlanMoveDto.toDomainOrNull(): TravelPlan.Move? = when (mode) {
    "walk" -> TravelPlan.Move(TravelPlan.Mode.WALK, minutes = min, km = km)
    // transit 은 소요 시간을 주지 않는다. 없는 값을 0 으로 채우지 않는다.
    "transit" -> TravelPlan.Move(TravelPlan.Mode.TRANSIT, minutes = null, km = km)
    else -> null
}

/** `/media/…` 는 우리 서버 이미지라 베이스 URL 을 붙인다. `http(s)://` 는 그대로 */
private fun absoluteUrl(raw: String, baseUrl: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    return baseUrl.trimEnd('/') + "/" + trimmed.trimStart('/')
}

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
