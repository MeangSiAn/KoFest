package com.mosstis.kofest.feature.festival.navigation

/** 하단 탭 4개. 기획서의 홈 · 목록 · 달력 · MY. */
enum class KoFestDestination(val route: String) {
    HOME("home"),
    LIST("list"),
    CALENDAR("calendar"),
    MY("my"),
    ;

    companion object {
        fun fromRoute(route: String?): KoFestDestination? =
            entries.firstOrNull { it.route == route?.substringBefore('?') }
    }
}
