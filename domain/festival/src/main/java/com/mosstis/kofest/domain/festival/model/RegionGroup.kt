package com.mosstis.kofest.domain.festival.model

/**
 * 홈의 "지역으로 찾기" 묶음 8개 (기획서 02).
 *
 * 서버 `/home` 의 `regions` 는 **시도 16~17개**를 그대로 준다. 그대로 늘어놓으면
 * 목록이 길고 "전남광주통합특별시" 같은 서버측 합성 이름이 그대로 노출된다.
 * 그래서 앱이 광역권으로 묶어 보여주고, 건수는 속한 시도를 더한다.
 *
 * 자동 일정의 권역(7개, [TravelPlanRequest.Area])과 **다르다** —
 * 여기서는 부산·울산을 경상에서 떼어낸다. 기획서가 그렇게 나눠 놓았다.
 */
enum class RegionGroup(val codes: List<String>) {
    SEOUL(listOf("11")),
    GYEONGGI_INCHEON(listOf("41", "28")),
    // 42 는 옛 강원도 코드. 서버는 51(강원특별자치도)로 주지만 옛 값이 섞여도 묶이게 둔다
    GANGWON(listOf("51", "42")),
    CHUNGCHEONG(listOf("43", "44", "30", "36")),
    // 12 는 서버가 쓰는 "전남광주통합특별시" 코드다 (2026-09-10 `/home` 응답으로 확인).
    // 실재하는 행정 코드가 아니라 수집 쪽 합성 값이라, 화면에 그대로 내보내지 않고 '전라'로 묶는다.
    JEOLLA(listOf("12", "46", "52", "45", "29")),
    GYEONGSANG(listOf("47", "48", "27")),
    JEJU(listOf("50")),
    BUSAN_ULSAN(listOf("26", "31")),
    ;

    companion object {
        /** 시도코드가 속한 묶음. 모르는 코드는 null — 묶지 못한 것을 임의로 넣지 않는다 */
        fun of(regionCode: String): RegionGroup? {
            val sido = regionCode.take(2)
            return entries.firstOrNull { sido in it.codes }
        }
    }
}
