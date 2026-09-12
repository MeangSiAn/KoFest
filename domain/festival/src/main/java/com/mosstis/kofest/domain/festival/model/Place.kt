package com.mosstis.kofest.domain.festival.model

/**
 * 관광지 한 건. `GET /places` 응답 (개발문서2 3장).
 *
 * 축제와 **같은 테이블을 쓰지 않는다** — 관광지에는 기간이 없고 수집 방식도 다르다.
 * 화면(상세)만 공유하고 데이터는 끝까지 갈라둔다.
 */
data class Place(
    val contentId: Long,
    val title: String,
    /** TourAPI `contentTypeId`. **언제나 한국어 번호**다 (12 관광지 · 14 문화시설 · 38 쇼핑) */
    val type: Int,
    val typeName: String?,
    /** 시도. 서버가 이름으로 완성해서 준다 */
    val region: String?,
    /** 주소에서 뽑은 구·시·군. `region` 만으로는 "서울특별시" 까지밖에 안 나온다 */
    val district: String?,
    val address: String?,
    val tel: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
) {
    /**
     * 목록 행에 쓰는 한 줄 주소. "서울특별시 종로구 사직로 161"
     *
     * 한국어 `addr` 은 이미 시도·시군구로 시작한다("경기도 화성시 석우동 29-3"). 그 앞에 또 붙이면
     * "경기도 화성시 경기도 화성시 …" 가 된다 (2026-09-12 실기기). 주소가 지역으로 시작하면 주소만 쓴다.
     */
    val fullAddress: String
        get() {
            val addr = address?.takeIf { it.isNotBlank() }
            val head = listOfNotNull(region, district).filter { it.isNotBlank() }
            if (addr != null && head.isNotEmpty() && addr.startsWith(head.first())) return addr
            return (head + listOfNotNull(addr)).joinToString(" ")
        }
}

/**
 * 관광지 상세. `detail` 은 **지금 항상 null 이다** — 소개글을 아직 수집하지 않았다.
 * null 이면 "자세한 소개는 준비 중입니다" 를 띄우고 주소와 지도로 대신한다.
 */
data class PlaceDetail(
    val place: Place,
    val overview: String?,
    /** 좌표로 계산한 주변 관광지. 추가 API 호출이 없다 */
    val nearby: List<Place>,
)

/** 관광지 목록 한 페이지. 커서는 `제목|contentId` 다 (정렬이 `title, content_id`) */
data class PlacePage(
    val items: List<Place>,
    val nextCursor: String?,
    val total: Int?,
)

/** 관광 탭 필터. `type` 은 한국어 번호로 보낸다 — 영문 번호(76)를 보내지 않는다 */
data class PlaceFilter(
    val regionCode: String? = null,
    val type: Int? = null,
    val hasImageOnly: Boolean = false,
    val query: String? = null,
)
