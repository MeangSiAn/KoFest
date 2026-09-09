package com.mosstis.kofest.data.festival.mapper

import com.mosstis.kofest.data.festival.remote.FestivalDetailResponse
import com.mosstis.kofest.domain.festival.model.DetailStatus
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.FestivalPhoto
import com.mosstis.kofest.domain.festival.model.Host
import com.mosstis.kofest.domain.festival.model.InfoItem
import com.mosstis.kofest.domain.festival.model.InfoKey

internal fun FestivalDetailResponse.toDomain(): FestivalDetail? {
    val start = startDate.toLocalDateOrNull() ?: return null
    val end = endDate.toLocalDateOrNull() ?: start

    val festival = Festival(
        contentId = contentId,
        title = title,
        region = region.orEmpty(),
        startDate = start,
        endDate = end,
        thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
        imageUrl = imageUrl?.takeIf { it.isNotBlank() } ?: thumbUrl.toOriginalImageUrlOrNull(),
        state = state.toFestivalStateOrUpcoming(),
    )

    val hero = festival.imageUrl ?: festival.thumbUrl

    return FestivalDetail(
        festival = festival,
        imageUrl = hero,
        address = addr?.takeIf { it.isNotBlank() },
        tel = tel?.takeIf { it.isNotBlank() },
        latitude = lat,
        longitude = lng,
        overview = overview?.takeIf { it.isNotBlank() },
        info = info.toInfoItems(),
        program = info[PROGRAM_KEY]?.takeIf { it.isNotBlank() },
        hosts = hosts
            .mapNotNull { host ->
                host.name?.takeIf { it.isNotBlank() }?.let { name ->
                    Host(name = name.trim(), tel = host.tel?.takeIf { it.isNotBlank() })
                }
            }
            // 주최1/주최2 가 같은 기관인 경우가 잦다 (전화가 있는 쪽만 남긴다).
            .groupBy { it.name }
            .map { (_, same) -> same.firstOrNull { it.tel != null } ?: same.first() },
        photos = photos(hero),
        homepageUrl = homepage.firstUrlOrNull(),
        nearby = nearby.toDomain(),
        detailStatus = detailStatus.toDetailStatus(),
    )
}

/**
 * 온 키만, 기획서의 표 순서대로 만든다. 없는 항목의 행은 만들지 않는다.
 * `program` 은 별도 섹션이라 여기서 뺀다.
 */
private fun Map<String, String>.toInfoItems(): List<InfoItem> =
    InfoKey.entries.mapNotNull { key ->
        this[key.apiKey]?.takeIf { it.isNotBlank() }?.let { InfoItem(key, it.trim()) }
    }

/**
 * 대표 이미지와 갤러리 첫 장이 같을 수 있다 — 중복을 제거한다.
 * 확장자(`.JPG` / `.jpg`)가 섞여 오므로 확장자로 판단하지 않고 URL 전체로 비교한다.
 */
private fun FestivalDetailResponse.photos(heroUrl: String?): List<FestivalPhoto> =
    images.mapNotNull { image ->
        val url = image.url?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        FestivalPhoto(
            url = url,
            thumbUrl = image.thumbUrl?.takeIf { it.isNotBlank() } ?: url,
            caption = image.caption?.takeIf { it.isNotBlank() },
        )
    }
        .distinctBy { it.url }
        .filterNot { it.url == heroUrl }

/**
 * `homepage` 는 `"공식 홈페이지 https://a\n공식 인스타그램 https://b"` 같은 자유 텍스트다.
 * 라벨을 그대로 열 수는 없으므로 첫 번째 http(s) URL 만 뽑는다.
 */
private fun String?.firstUrlOrNull(): String? =
    this?.let { URL_PATTERN.find(it)?.value?.trimEnd('.', ',', ')') }

private val URL_PATTERN = Regex("""https?://\S+""")

private const val PROGRAM_KEY = "program"

/**
 * 모르는 값이면 OK 로 둔다 — 임의로 '수집 안 됨' 안내를 띄우면
 * 실제로 있는 정보 위에 잘못된 설명이 붙는다.
 */
private fun String?.toDetailStatus(): DetailStatus = when (this) {
    "none" -> DetailStatus.NONE
    "partial" -> DetailStatus.PARTIAL
    else -> DetailStatus.OK
}
