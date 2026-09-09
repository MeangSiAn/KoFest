package com.mosstis.kofest.domain.festival.model

/** 서버가 정하는 진행 상태. 앱에서 오늘 날짜와 비교해 다시 계산하지 않는다. */
enum class FestivalState {
    ONGOING,
    UPCOMING,
    ENDED,
}
