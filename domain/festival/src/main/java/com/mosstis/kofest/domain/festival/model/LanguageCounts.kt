package com.mosstis.kofest.domain.festival.model

import com.mosstis.kofest.core.common.AppLanguage

/**
 * 언어별 건수. 목록·홈 응답에 실려 온다.
 *
 * "더 많은 정보가 있습니다"보다 "237 vs 889"가 설득력 있다 — 숫자를 실제로 노출한다.
 *
 * 서버가 **언어마다 축제·관광지를 따로** 준다 (`counts.ko.festival` · `counts.ko.place`).
 * 예전에는 `{"ko": 903, "en": 235}` 처럼 평면이었다.
 */
data class LanguageCounts(
    val ko: CountBreakdown,
    val en: CountBreakdown,
) {
    /** 지금 보고 있는 언어의 건수 */
    fun of(language: AppLanguage): CountBreakdown = when (language) {
        AppLanguage.KO -> ko
        AppLanguage.EN -> en
    }

    companion object {
        val EMPTY = LanguageCounts(CountBreakdown.EMPTY, CountBreakdown.EMPTY)
    }
}

data class CountBreakdown(
    val festival: Int,
    val place: Int,
    val total: Int,
) {
    companion object {
        val EMPTY = CountBreakdown(festival = 0, place = 0, total = 0)
    }
}
