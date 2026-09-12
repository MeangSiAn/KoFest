package com.mosstis.kofest.domain.festival.analytics

/**
 * `POST /events` 가 받는 이벤트 이름. 서버 계약이라 문자열을 마음대로 바꾸지 않는다.
 *
 * 통계는 **미루지 않고 처음부터 붙인다.** 나중에 붙이면 초기 사용자의 행동을 영영 모른다
 * (개발문서 9장).
 */
enum class AppEvent(val eventName: String) {
    // 화면
    APP_OPEN("app_open"),
    VIEW_HOME("view_home"),
    VIEW_LIST("view_list"),
    VIEW_DETAIL("view_detail"),

    // 행동
    SAVE("save"),
    UNSAVE("unsave"),
    SEARCH("search"),
    FILTER_REGION("filter_region"),

    // 기타
    BANNER_VIEW("banner_view"),
    BANNER_CLICK("banner_click"),
    OUTLINK("outlink"),
    LANG_SWITCH("lang_switch"),
}
