package com.mosstis.kofest.domain.festival.model

/**
 * 오늘 뭐하지 — `GET /api/pick` 한 번의 결과 (기획서 11).
 *
 * [place] 가 null 이면 둘 중 하나다. [exhausted] 가 true 면 반경 안을 **다 본 것**이고,
 * false 면 그 반경에 **아무것도 없는 것**이다. 문구가 달라야 하므로 합치지 않는다.
 */
data class PickResult(
    val place: PickedPlace?,
    /** 이번에 쓴 반경. 10 또는 20 — 10km 를 다 보면 서버가 알아서 넓힌다 */
    val km: Int,
    /** 그 반경 안의 후보 수 */
    val total: Int,
    /** 지금까지 본 개수. "24곳 중 3번째" */
    val seen: Int,
    val exhausted: Boolean,
)

/** 뽑힌 한 곳. 거리는 서버가 좌표로 계산해 준다 */
data class PickedPlace(
    val place: Place,
    val km: Double,
)
