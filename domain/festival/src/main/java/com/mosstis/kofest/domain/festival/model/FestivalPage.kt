package com.mosstis.kofest.domain.festival.model

/**
 * 커서 페이징 결과.
 *
 * offset 을 쓰지 않는다 — 매일 04:00 배치로 데이터가 갱신되면 어긋난다.
 * 커서는 `"2026-10-15|293084"` 형태이고 정렬은 `start_date, content_id` 고정이다.
 *
 * 서버는 전체 건수를 내려주지 않는다. 화면에 필요한 건수는 [counts] 로 대신한다.
 */
data class FestivalPage(
    val items: List<Festival>,
    val nextCursor: String?,
    val counts: LanguageCounts,
)
