package com.mosstis.kofest.domain.festival.model

/**
 * 홈 상단 배너 한 장. **서버 관리자 화면에서 등록한 것만** 쓴다 (`GET /home` 의 `banners`).
 *
 * 응답 배열의 **순서가 곧 노출 순서**다. id 순이 아니다 — 정렬하거나 걸러내지 않는다.
 *
 * 이미지 말고는 전부 없을 수 있다. 실제 응답(2026-09-08)에 `imageUrl`·`imageAlt` 만 있고
 * `kicker`·`title`·`subtitle` 이 통째로 빠진 배너가 있다. 그런 배너는 이미지만 보여준다.
 */
data class Banner(
    val id: String,
    val imageUrl: String,
    /** 스크린리더가 읽을 설명 */
    val imageAlt: String?,
    val kicker: String?,
    val title: String?,
    val subtitle: String?,
    val link: Link?,
) {
    /** 이미지 위에 얹을 글이 하나라도 있는가. 없으면 어둡게 덮지 않는다 */
    val hasText: Boolean
        get() = !kicker.isNullOrBlank() || !title.isNullOrBlank() || !subtitle.isNullOrBlank()

    /** 배너를 눌렀을 때 갈 곳. `linkType: "none"` 이거나 모르는 값이면 null 이라 눌러도 아무 일 없다 */
    sealed interface Link {
        data class Url(val url: String) : Link
        data class FestivalDetail(val contentId: Long) : Link
    }
}
