package com.mosstis.kofest.domain.festival.model

import java.time.LocalDate

/**
 * `GET /home` 한 번으로 채우는 홈 화면 묶음.
 * 홈에서 5번 호출하면 느리다 — 하나로 묶는다.
 */
data class HomeFeed(
    /** 서버 관리자 배너. **응답 순서가 곧 노출 순서**다 */
    val banners: List<Banner>,
    /** 오늘 열려 있는 축제. 0건이면 섹션 자체를 숨긴다 */
    val ongoing: List<Festival>,
    /** 주말에 열려 있는 축제 */
    val weekend: List<Festival>,
    /** 서버가 정한 주말 범위 (`weekend.from` ~ `weekend.to`) */
    val weekendRange: ClosedRange<LocalDate>,
    /** 금~일이면 서버가 다음 주말을 준다. 제목이 '다음 주말'로 바뀐다 */
    val weekendIsNext: Boolean,
    val regions: List<RegionBucket>,
    /** 현재 언어의 전체 축제 수. `counts` 에서 온다 */
    val totalCount: Int,
    val counts: LanguageCounts,
)
