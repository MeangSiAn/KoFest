package com.mosstis.kofest.domain.festival.model

import java.time.LocalDate

/**
 * 목록/홈에 쓰는 축제 한 건.
 *
 * `GET /festivals` 응답 1건과 1:1 로 맞춘다 (`docs/05_남은일.md`).
 * 화면에 안 쓰는 필드는 넣지 않는다.
 *
 * 두 가지는 **서버가 완성해서 내려주고 앱이 계산하지 않는다.**
 * - [region] — 서버가 지역코드를 이름으로 바꿔서 준다
 * - [state] — 기기 시간이 틀어져 있으면 표시가 어긋나므로 서버가 정한다
 */
data class Festival(
    val contentId: Long,
    val title: String,
    val region: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    /** 목록 썸네일(`firstimage2`, 300×200). 목록 행처럼 작은 자리에 쓴다 */
    val thumbUrl: String?,
    /**
     * 원본(`firstimage`). 배너·카드처럼 큰 자리에 쓴다.
     * 서버가 주면 그 값, 아니면 data 레이어가 썸네일 URL 에서 유도한다. 화면은 실패 시 [thumbUrl] 로 되돌아간다.
     */
    val imageUrl: String?,
    val state: FestivalState,
) {
    val hasImage: Boolean get() = !thumbUrl.isNullOrBlank()

    val isSingleDay: Boolean get() = startDate == endDate
}
