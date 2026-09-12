package com.mosstis.kofest.domain.festival.model


/**
 * `GET /home` 한 번으로 채우는 홈 화면 묶음.
 * 홈에서 5번 호출하면 느리다 — 하나로 묶는다.
 */
data class HomeFeed(
    /** 서버 관리자 배너. **응답 순서가 곧 노출 순서**다 */
    val banners: List<Banner>,
    /** 오늘 열려 있는 축제. 0건이면 섹션 자체를 숨긴다 */
    val ongoing: List<Festival>,
    /** "지금 떠나기 좋은 곳" 캐러셀. 서버가 안 주면 비어 있고 섹션을 통째로 뺀다 */
    val themes: List<Theme>,
    /** "가볼 만한 곳" 관광지 8건 */
    val picks: List<Place>,
    /** 0 이면 매거진 섹션과 탭을 그리지 않는다 (개발문서2 3장) */
    val storyCount: Int,
    val themeCount: Int,
    val regions: List<RegionBucket>,
    /** 현재 언어의 전체 축제 수. `counts` 에서 온다 */
    val totalCount: Int,
    val counts: LanguageCounts,
)
