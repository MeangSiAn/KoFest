package com.mosstis.kofest.domain.festival.model

/**
 * 홈의 '지역으로 찾기' 한 칸.
 *
 * [code] 는 시도코드 2자리, [name] 은 **서버가 완성해서 준 이름**이다
 * (`lang=en` 이면 "Seoul"). 앱이 코드를 이름으로 바꾸지 않는다.
 *
 * 기획서의 8개 광역권(경기·인천, 부산·울산 …)은 어디에도 정의가 없는 임의 묶음이라
 * 서버가 묶어 주기 전까지는 시도 단위를 그대로 보여준다.
 */
data class RegionBucket(
    val code: String,
    val name: String,
    val count: Int,
)
