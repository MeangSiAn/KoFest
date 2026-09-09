package com.mosstis.kofest.domain.festival.model

import java.time.LocalDate

/** 목록 화면의 필터. 그대로 `GET /festivals` 쿼리 파라미터가 된다. */
data class FestivalFilter(
    /** 시도코드 2자리 또는 시군구코드 5자리. null 이면 전국 */
    val regionCode: String? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val ongoingOnly: Boolean = false,
    val hasImageOnly: Boolean = false,
)
