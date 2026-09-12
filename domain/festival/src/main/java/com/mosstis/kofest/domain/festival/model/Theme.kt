package com.mosstis.kofest.domain.festival.model

/**
 * 테마 — "골라 담은 여행 코스". `GET /themes` 또는 `GET /home` 의 `themes`.
 *
 * **앱에는 테마 화면이 없다.** 홈 캐러셀에서 카드를 누르면 그 자리에서 아래로 펼쳐진다.
 * 항목까지 한 번에 오므로 펼칠 때 다시 부르지 않는다 (기획서 08).
 */
data class Theme(
    val id: Long,
    /** 카드 위 작은 라벨. "가을 고궁" */
    val badge: String?,
    val title: String,
    val subtitle: String?,
    /** `/media/` 로 시작하면 우리 서버 이미지라 베이스 URL 을 붙인다 */
    val coverUrl: String?,
    val count: Int,
    val items: List<Item>,
) {
    /**
     * 테마에 담긴 것 하나. 축제와 관광지가 섞인다 —
     * **언제든 갈 수 있는 곳과 그날뿐인 것**이라 화면에서 색으로 구분한다.
     */
    data class Item(
        val kind: Kind,
        val contentId: Long,
        val title: String,
        val region: String?,
        val thumbUrl: String?,
        /** 축제일 때만 있다 */
        val period: ClosedRange<java.time.LocalDate>?,
    )

    /** 모르는 kind 는 아예 만들지 않는다 — 서버가 종류를 늘려도 옛 앱이 안 깨진다 */
    enum class Kind { FESTIVAL, PLACE }
}
