package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable

/**
 * `GET /places` · `GET /places/{lang}/{contentId}` (개발문서2 3장).
 *
 * 서버가 아직 앱용 JSON 을 내려주지 않는다 — 2026-09-10 기준 이 경로는 **HTML(웹 페이지)** 이다.
 * 스펙이 문서에 확정되어 있어 먼저 맞춰 두고, 서버가 열리면 그대로 붙는다.
 */
@Serializable
data class PlaceListResponse(
    val items: List<PlaceDto> = emptyList(),
    val nextCursor: String? = null,
    val total: Int? = null,
)

@Serializable
data class PlaceDto(
    val contentId: Long = 0,
    val title: String = "",
    /** 언제나 한국어 번호 (12 관광지 · 14 문화시설 · 38 쇼핑) */
    val type: Int = 0,
    val typeName: String? = null,
    val region: String? = null,
    /** 주소에서 뽑은 구·시·군 */
    val district: String? = null,
    val addr: String? = null,
    val tel: String? = null,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class PlaceDetailResponse(
    val place: PlaceDto,
    /** **지금 항상 null 이다.** 관광지 소개글을 아직 수집하지 않았다 */
    val detail: PlaceOverviewDto? = null,
    val nearby: List<PlaceDto> = emptyList(),
)

@Serializable
data class PlaceOverviewDto(
    val overview: String? = null,
)

/**
 * `GET /api/pick` (개발문서 · 기획서 11). 2026-09-12 실제 응답으로 확인했다.
 *
 * `place` 가 null 이면 `exhausted` 로 이유가 갈린다 — true 는 다 본 것, false 는 반경 안에 없는 것.
 */
@Serializable
data class PickResponse(
    val place: PickPlaceDto? = null,
    val km: Int = 0,
    val total: Int = 0,
    val seen: Int = 0,
    val exhausted: Boolean = false,
)

/** [PlaceDto] 에 거리(`km`)가 붙은 것. 서버가 좌표로 계산해 준다 */
@Serializable
data class PickPlaceDto(
    val contentId: Long = 0,
    val title: String = "",
    val type: Int = 0,
    val typeName: String? = null,
    val region: String? = null,
    val district: String? = null,
    val addr: String? = null,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val km: Double = 0.0,
)
