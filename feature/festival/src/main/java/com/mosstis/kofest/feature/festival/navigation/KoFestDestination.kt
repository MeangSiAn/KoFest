package com.mosstis.kofest.feature.festival.navigation

/**
 * 하단 탭 — 홈 · 목록 · 일정 · 매거진 · MY (기획서 v1).
 *
 * 달력은 탭이 아니다. 기획서 05 가 목록 화면 안의 `목록 | 달력` 전환으로 옮겼다 —
 * 둘 다 날짜로 보는 화면이라 성격이 겹치고, 같은 필터를 공유해야 하기 때문이다.
 */
enum class KoFestDestination(val route: String) {
    HOME("home"),
    LIST("list"),
    PLAN("plan"),
    MAGAZINE("magazine"),
    MY("my"),
    ;

    companion object {
        fun fromRoute(route: String?): KoFestDestination? =
            entries.firstOrNull { it.route == route?.substringBefore('?') }
    }
}
