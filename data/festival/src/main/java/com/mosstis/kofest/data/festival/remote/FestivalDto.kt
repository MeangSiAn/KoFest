package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable

/**
 * `https://kofest.mosstis.com` 실제 응답을 그대로 옮긴 것이다. 추측한 필드는 없다.
 *
 * 확인된 사항
 *   GET /festivals   → { items[], counts{ko,en}, nextCursor }
 *   GET /home        → { ads, notice, minAppVersion, banners[], ongoing[], themes[], picks[], storyCount, themeCount, regions[], counts }
 *   인증             → `X-API-Key` 헤더. 실패 시 401 + { error, message, hint }
 *   목록 정렬        → start_date, content_id 오름차순
 *   기본 필터        → 종료된 축제는 오지 않는다 (서버가 오늘 기준으로 거른다)
 *
 * 값이 없으면 key 자체가 오지 않을 수 있으므로 nullable 인 것에는 기본값을 준다.
 */
@Serializable
data class FestivalDto(
    val contentId: Long,
    val title: String,
    val region: String? = null,
    val startDate: String,
    val endDate: String,
    val thumbUrl: String? = null,
    /** 아직 목록/홈 응답에는 없다. 서버가 추가하면 그대로 쓴다 */
    val imageUrl: String? = null,
    val state: String? = null,
)

@Serializable
data class CountsDto(
    val ko: CountDto = CountDto(),
    val en: CountDto = CountDto(),
)

/**
 * 언어 하나의 건수. 서버가 축제·관광지를 나눠서 준다.
 * 예전 평면 구조(`"ko": 903`)에서 바뀌었다 — 평면으로 두면 파싱이 통째로 실패한다.
 */
@Serializable
data class CountDto(
    val festival: Int = 0,
    val place: Int = 0,
    val total: Int = 0,
)

@Serializable
data class FestivalListResponse(
    val items: List<FestivalDto> = emptyList(),
    val counts: CountsDto = CountsDto(),
    val nextCursor: String? = null,
)


@Serializable
data class RegionDto(
    val code: String,
    val name: String,
    val count: Int = 0,
)

/**
 * 관리자 화면에서 등록한 배너. 2026-09-08 실제 응답:
 * `{id, imageUrl:"/media/banners/2026/09/ko-….jpg", imageAlt, linkType:"url", source:"managed",
 *   kicker, title, subtitle, linkValue:"www.naver.com"}`
 * `imageUrl` 은 상대 경로, `linkValue` 는 스킴이 없을 수 있다.
 */
@Serializable
data class BannerDto(
    val id: Long,
    val imageUrl: String? = null,
    val imageAlt: String? = null,
    val linkType: String? = null,
    val source: String? = null,
    val kicker: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val linkValue: String? = null,
)

@Serializable
data class HomeResponse(
    /** 이 버전 미만이면 업데이트를 알린다. 실제 응답은 아직 null */
    val minAppVersion: String? = null,
    val ads: AdsDto? = null,
    val banners: List<BannerDto> = emptyList(),
    val ongoing: List<FestivalDto> = emptyList(),
    /** 테마 캐러셀. `/themes` 를 따로 안 불러도 된다 */
    val themes: List<ThemeDto> = emptyList(),
    /** 관광지 8건. 무작위이되 하루 동안 같다 */
    val picks: List<PlaceDto> = emptyList(),
    /** 0 이면 그 섹션·탭을 그리지 않는다 */
    val storyCount: Int = 0,
    val themeCount: Int = 0,
    val regions: List<RegionDto> = emptyList(),
    val counts: CountsDto = CountsDto(),
)

/**
 * 광고 설정. **개발문서 3장과 필드 이름이 다르다** — 문서는 `listFirst`/`unitList`/`unitDetail`,
 * 실제 응답(2026-09-10)은 `{enabled, listFirstIndex, listInterval, onDetail}` 이고 광고 단위 ID 는 없다.
 * 응답을 기준으로 맞춘다.
 */
@Serializable
data class AdsDto(
    val enabled: Boolean = false,
    val listFirstIndex: Int = 0,
    val listInterval: Int = 0,
    val onDetail: Boolean = false,
)

/**
 * `GET /health`. 언어별 건수를 얻는 가장 싼 호출이라 인트로가 이걸 쓴다.
 * (`ok`, `regions`, `lastSync` 등 나머지 필드는 앱에서 쓰지 않는다.)
 */
@Serializable
data class HealthResponse(
    val ok: Boolean = false,
    val counts: CountsDto = CountsDto(),
)

/** 401 등 실패 응답. 원인을 그대로 사용자/로그에 남기기 위해 파싱한다. */
@Serializable
data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val hint: String? = null,
)

/**
 * 상세 응답. 실제 응답 25건(ko) + 25건(en)을 확인하고 만든 것이다.
 *
 * - `info` 는 **빈 값의 키 자체가 오지 않는다.** 그래서 고정 필드가 아니라 Map 으로 받는다
 *   (관측된 키: place, playTime, fee, program, ageLimit, spendTime, discount)
 * - 영어는 `info` 와 `hosts` 가 25/25 비어 있고 overview·imageUrl·tel 도 자주 null 이다
 * - `homepage` 는 URL 하나가 아니라 라벨이 섞인 자유 텍스트다
 *   (예: `"공식 홈페이지 https://... \n공식 인스타그램 https://..."`)
 * - `nearby` 항목은 목록 아이템과 같은 필드다
 */
@Serializable
data class FestivalDetailResponse(
    val contentId: Long,
    val title: String,
    val region: String? = null,
    val startDate: String,
    val endDate: String,
    val thumbUrl: String? = null,
    val state: String? = null,
    val addr: String? = null,
    val tel: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val imageUrl: String? = null,
    val overview: String? = null,
    val homepage: String? = null,
    val info: Map<String, String> = emptyMap(),
    val hosts: List<HostDto> = emptyList(),
    val images: List<FestivalImageDto> = emptyList(),
    val nearby: List<FestivalDto> = emptyList(),
    val detailStatus: String? = null,
)

@Serializable
data class HostDto(
    val name: String? = null,
    val tel: String? = null,
)

@Serializable
data class FestivalImageDto(
    val url: String? = null,
    val thumbUrl: String? = null,
    val caption: String? = null,
)
