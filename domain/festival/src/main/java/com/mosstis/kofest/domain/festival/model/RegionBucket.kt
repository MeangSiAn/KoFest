package com.mosstis.kofest.domain.festival.model

/**
 * 홈 '지역으로 찾기' 한 칸. `/home` 의 `regions` 한 건이다.
 *
 * **시도 단위다.** 자동 일정 짜기의 광역권 9개(`seoul` `chungcheong` …)와는 다른 목록이고
 * 섞으면 안 된다 — 목록 필터가 시도 단위라 묶으면 필터와 안 맞는다.
 * 서버가 주는 대로 **전부** 그린다. 자르지 않는다.
 *
 * [name] 은 서버가 완성해서 준 이름이다 (`lang=en` 이면 "Seoul").
 * "서울특별시"를 "서울"로 줄이지 않는다.
 */
data class RegionBucket(
    val code: String,
    val name: String,
    /** 앞으로 열릴 축제 수 */
    val count: Int,
    /** 관광지 수. 축제만 세면 관광지 1만 건이 없는 것처럼 보인다 */
    val places: Int,
)
