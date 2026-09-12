package com.mosstis.kofest.domain.festival.model

import java.time.LocalDate

/**
 * 목록 화면의 필터. 그대로 `GET /festivals` 쿼리 파라미터가 된다.
 *
 * [regionCodes] 가 여럿인 것은 홈의 광역권(경기·인천 = 41 + 28) 때문이다.
 * **서버는 지역을 하나만 받는다** (`region=41,28` 은 0건, `region=41&region=28` 은 뒤엣것만).
 * 그래서 여럿이면 data 계층이 코드별로 부른 뒤 합친다.
 */
data class FestivalFilter(
    /** 시도코드 2자리 또는 시군구코드 5자리. 비어 있으면 전국 */
    val regionCodes: List<String> = emptyList(),
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val ongoingOnly: Boolean = false,
    val hasImageOnly: Boolean = false,
    /** 축제 이름 검색. `/festivals?q=` */
    val query: String? = null,
) {
    /** 화면 제목처럼 "한 지역"으로 다룰 때 쓴다. 광역권이면 첫 코드다 */
    val primaryRegionCode: String? get() = regionCodes.firstOrNull()
}
