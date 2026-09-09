package com.mosstis.kofest.data.festival.mapper

/**
 * 썸네일 URL 에서 원본 URL 을 만든다.
 *
 * 목록/홈 응답에는 원본(`firstimage`)이 없고 썸네일(`firstimage2`)만 온다. 썸네일은 300×200 고정이라
 * 배너(338×250dp)에서는 해상도가 1/4 이고, 홈 카드(168×210dp 세로)에서는 가로 사진을 세로로 크롭까지 한다.
 *
 * TourAPI 파일명 규칙은 `..._image2_1.jpg` 가 원본, `..._image3_1.jpg` 가 썸네일이다.
 * 확인한 근거 (2026-09-07):
 *  - 목록 20건 모두 `_image3_` → `_image2_` 치환 URL 이 HTTP 200 으로 존재
 *  - 그중 5건은 상세 API 가 내려주는 `imageUrl` 과 문자열이 정확히 일치
 * 903건 전체를 검증한 것은 아니다. 그래서 화면은 이 URL 이 실패하면 썸네일로 되돌아간다.
 *
 * 서버가 목록에 `imageUrl` 을 내려주기 시작하면 이 함수는 호출되지 않는다 (서버 값이 우선).
 */
internal fun String?.toOriginalImageUrlOrNull(): String? {
    val thumb = this?.takeIf { it.isNotBlank() } ?: return null
    if (!thumb.contains(THUMB_MARKER)) return null
    return thumb.replace(THUMB_MARKER, ORIGINAL_MARKER)
}

private const val THUMB_MARKER = "_image3_"
private const val ORIGINAL_MARKER = "_image2_"
