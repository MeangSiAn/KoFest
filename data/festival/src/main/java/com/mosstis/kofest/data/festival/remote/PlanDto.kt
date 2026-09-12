package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable

/**
 * `GET /plan` (개발문서2 3장, 기획서 09).
 *
 * 2026-09-10 기준 이 경로는 **HTML(웹 페이지)** 이다. 앱용 JSON 이 열리면 그대로 붙는다.
 */
@Serializable
data class PlanResponse(
    /** 실제 응답에 `title` 은 없다. 제목은 앱이 `areaName` + 박수로 만든다 (기획서 09 "서울 2박 3일") */
    val areaName: String? = null,
    val moodName: String? = null,
    val total: Int = 0,
    val festivalCount: Int = 0,
    /** true 면 "이 지역은 아직 관광지 정보가 적습니다" */
    val thin: Boolean = false,
    val days: List<PlanDayDto> = emptyList(),
)

@Serializable
data class PlanDayDto(
    val date: String? = null,
    val label: String? = null,
    val stops: List<PlanStopDto> = emptyList(),
)

@Serializable
data class PlanStopDto(
    /** `festival` `place` `meal` — 모르는 kind 는 건너뛴다 */
    val kind: String? = null,
    val time: String? = null,
    val contentId: Long? = null,
    val title: String? = null,
    /** `meal` 은 title 대신 label 로 온다 ("저녁") */
    val label: String? = null,
    val region: String? = null,
    /** `meal` 은 region 대신 where 로 온다 */
    val where: String? = null,
    val stay: Int? = null,
    val thumbUrl: String? = null,
    val move: PlanMoveDto? = null,
)

@Serializable
data class PlanMoveDto(
    /** `walk` 면 [min] 이 있고, `transit` 이면 **null 이다** */
    val mode: String? = null,
    val min: Int? = null,
    val km: Double? = null,
)
