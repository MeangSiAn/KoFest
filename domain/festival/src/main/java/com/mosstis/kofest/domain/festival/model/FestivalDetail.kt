package com.mosstis.kofest.domain.festival.model

/**
 * `GET /festivals/{lang}/{contentId}` 한 번으로 오는 상세.
 * 소개·안내·사진·주변 축제가 모두 여기 들어 있어 추가 호출이 없다.
 *
 * **데이터가 얇다는 것을 전제로 한다.** 실제 응답을 25건씩 확인한 결과:
 * - 한국어: `info` 는 place/playTime/fee/program 은 대부분 있고 ageLimit 17/25, spendTime 8/25, discount 1/25
 * - 영어: `info` 와 `hosts` 가 **25건 모두 비어 있고** overview 7/25·imageUrl 7/25 가 null
 *
 * 그래서 비어 있는 값은 이 모델에서 아예 빠진다. 화면은 있는 것만 그리고,
 * 비어 있는 섹션은 제목까지 통째로 숨긴다.
 */
data class FestivalDetail(
    val festival: Festival,
    /** 대표 이미지 원본(`firstimage`). 목록의 썸네일과 다르다 */
    val imageUrl: String?,
    val address: String?,
    val tel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val overview: String?,
    /** 안내 표. 값이 있는 항목만, 기획서 순서대로 들어 있다 */
    val info: List<InfoItem>,
    /** `info.program` 은 별도 섹션이라 따로 뺀다. 줄바꿈을 그대로 살려야 읽힌다 */
    val program: String?,
    val hosts: List<Host>,
    val photos: List<FestivalPhoto>,
    /** 응답의 `homepage` 는 라벨이 섞인 자유 텍스트라, 첫 URL 만 뽑아 둔 것 */
    val homepageUrl: String?,
    val nearby: List<Festival>,
    val detailStatus: DetailStatus,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null
}

/**
 * "정보가 없는 축제"와 "아직 수집 안 된 축제"를 구분하기 위해 서버가 내려준다.
 * 전자는 그냥 없는 것이고, 후자는 며칠 뒤 다시 보면 채워져 있다.
 */
enum class DetailStatus {
    OK,
    PARTIAL,

    /** 상세 배치가 아직 이 축제를 안 받았다 */
    NONE,
}

/** 주최 · 주관 한 줄. [tel] 은 없을 수 있다 */
data class Host(
    val name: String,
    val tel: String?,
)

data class FestivalPhoto(
    val url: String,
    val thumbUrl: String,
    val caption: String?,
)
