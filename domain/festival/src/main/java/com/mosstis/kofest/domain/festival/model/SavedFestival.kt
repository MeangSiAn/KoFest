package com.mosstis.kofest.domain.festival.model

import com.mosstis.kofest.core.common.AppLanguage

/**
 * 저장한 축제. **기기 로컬에만 둔다** — 로그인을 요구하지 않는다.
 * 외국인 관광객이 여행 중 잠깐 쓰는 앱에서 회원가입은 이탈 지점이다.
 *
 * 언어별로 contentId 가 다르므로 키는 `(language, contentId)` 다.
 */
data class SavedFestival(
    val language: AppLanguage,
    val festival: Festival,
    val savedAtEpochMillis: Long,
) {
    val key: String get() = "${language.code}:${festival.contentId}"
}
