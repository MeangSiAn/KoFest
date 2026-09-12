package com.mosstis.kofest.data.festival.remote

import kotlinx.serialization.Serializable

/**
 * `POST /events` 요청 본문. 개발문서 3장의 형태 그대로다.
 *
 * ```json
 * {"deviceId": "…", "appVersion": "1.0.0", "lang": "ko",
 *  "events": [{"event": "view_detail", "contentId": 293084, "at": "…"}]}
 * ```
 *
 * 국가는 보내지 않는다 — 서버가 Cloudflare 헤더로 채운다.
 */
@Serializable
data class EventBatchRequest(
    val deviceId: String,
    val appVersion: String,
    val lang: String,
    val events: List<EventDto>,
)

/**
 * 응답. `{"accepted": 2}` — 보낸 건수와 받은 건수를 맞춰볼 수 있다.
 * (2026-09-10 실제 요청으로 확인)
 */
@Serializable
data class EventAcceptedResponse(
    val accepted: Int = 0,
)

@Serializable
data class EventDto(
    val event: String,
    val contentId: Long? = null,
    /** ISO-8601 UTC. `Instant.toString()` 그대로 */
    val at: String,
)
