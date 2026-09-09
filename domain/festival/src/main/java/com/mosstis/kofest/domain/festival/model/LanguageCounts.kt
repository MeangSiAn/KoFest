package com.mosstis.kofest.domain.festival.model

/**
 * 언어별 축제 건수. 모든 목록 응답에 실려 온다.
 *
 * "더 많은 정보가 있습니다"보다 "235 vs 903"이 설득력 있다 — 숫자를 실제로 노출한다.
 */
data class LanguageCounts(
    val ko: Int,
    val en: Int,
)
