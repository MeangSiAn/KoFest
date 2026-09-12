package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable

/**
 * `GET /home` 의 `themes`. **별도 엔드포인트는 없다** — 앱에 테마 화면이 없으므로 따로 부를 이유가 없다 (개발문서3).
 * `badge` `subtitle` `coverUrl` 은 비어 있으면 키 자체가 안 온다.
 */
@Serializable
data class ThemeDto(
    val id: Long = 0,
    val badge: String? = null,
    val title: String = "",
    val subtitle: String? = null,
    /** `/media/` 로 시작하면 베이스 URL 을 붙인다 */
    val coverUrl: String? = null,
    val count: Int = 0,
    val items: List<ThemeItemDto> = emptyList(),
)

@Serializable
data class ThemeItemDto(
    /** `festival` 또는 `place`. 모르는 값이면 그 항목을 버린다 */
    val kind: String? = null,
    val contentId: Long = 0,
    val title: String = "",
    val region: String? = null,
    val thumbUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)
