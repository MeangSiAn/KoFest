package com.mosstis.kofest.domain.festival.model

/**
 * 검색 결과. **축제와 관광지를 함께** 찾는다.
 *
 * 사람은 "경복궁"을 칠 때 그게 축제인지 관광지인지 생각하지 않는다.
 * 전용 검색 API 가 없어 `/festivals?q=` 와 `/api/places?q=` 를 나란히 부르고 여기서 묶는다.
 *
 * 한쪽이 실패해도 다른 쪽은 보여준다 — 둘 다 죽어야 실패다.
 */
data class SearchResult(
    val query: String,
    val festivals: List<Festival>,
    val places: List<Place>,
    /** 축제 쪽 조회가 실패했는가. 결과 0건과 구분한다 */
    val festivalFailed: Boolean = false,
    val placeFailed: Boolean = false,
) {
    val isEmpty: Boolean get() = festivals.isEmpty() && places.isEmpty()

    /** 둘 다 실패했으면 "못 불러왔다", 하나라도 성공이면 결과 화면 */
    val allFailed: Boolean get() = festivalFailed && placeFailed
}
