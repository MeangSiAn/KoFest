package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * `GET /stories` · `GET /stories/{lang}/{slug}` (개발문서2 3장).
 *
 * 2026-09-10 기준 `/stories` 는 **404** 다. 스펙대로 먼저 맞춰 둔다.
 */
@Serializable
data class StoryListResponse(
    val items: List<StoryDto> = emptyList(),
    val nextCursor: String? = null,
    val total: Int? = null,
)

@Serializable
data class StoryDto(
    val id: Long = 0,
    val slug: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val coverUrl: String? = null,
    val publishedAt: String? = null,
    val updatedAt: String? = null,
    val pinned: Boolean = false,
)

@Serializable
data class StoryContentResponse(
    val story: StoryDto,
    val blocks: List<StoryBlockDto> = emptyList(),
)

/**
 * 본문 블록. 타입마다 쓰는 필드가 달라 **하나의 관대한 DTO** 로 받는다.
 *
 * 다형 직렬화(sealed + discriminator)를 쓰면 서버가 새 타입을 추가했을 때 통째로 파싱이 깨진다.
 * 문서가 "모르는 type 은 건너뛴다" 를 요구하므로 여기서 버리는 쪽이 맞다.
 */
@Serializable
data class StoryBlockDto(
    val type: String? = null,
    /** `p` `h2` `h3` `quote` — 안에 `\n` 과 `**굵게**` · `[글자](주소)` 가 올 수 있다 */
    val text: String? = null,
    /** `ul` `ol` */
    val items: List<String>? = null,
    /** `image` — 절대 주소로 온다 */
    val url: String? = null,
    val alt: String? = null,
    /** `festival` `place` 카드 */
    val contentId: Long? = null,
    val title: String? = null,
    val region: String? = null,
    val thumbUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    /** 앞으로 늘어날 필드를 삼키지 않기 위한 자리 — 파싱을 깨뜨리지 않는다 */
    val extra: JsonElement? = null,
)
