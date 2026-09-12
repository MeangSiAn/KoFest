package com.mosstis.kofest.domain.festival.model

import java.time.Instant
import java.time.LocalDate

/** 매거진 글 한 건(목록용). 정렬은 `pinned DESC, publishedAt DESC, id` 로 서버가 준다 */
data class Story(
    val id: Long,
    val slug: String,
    val title: String,
    val excerpt: String?,
    /** `/media/` 로 시작하면 베이스 URL 을 붙인다. 없으면 제목 첫 글자를 자주색 면에 얹는다 */
    val coverUrl: String?,
    val publishedAt: Instant?,
    /** 맨 위에 고정. 화면에 "추천" 표가 붙는다 */
    val pinned: Boolean,
)

data class StoryPage(
    val items: List<Story>,
    val nextCursor: String?,
    val total: Int?,
)

/**
 * 매거진 본문. **HTML 이 아니라 블록 배열**로 온다.
 *
 * WebView 로 그리면 글꼴·여백이 앱과 따로 놀고, 축제를 누르면 브라우저가 열려 앱을 떠난다.
 * 블록으로 받으면 축제·관광지 카드를 **앱 안의 상세로** 보낼 수 있다 (기획서 07).
 */
data class StoryContent(
    val story: Story,
    val blocks: List<StoryBlock>,
)

/**
 * 본문 블록. **모르는 type 은 아예 만들지 않고 건너뛴다** —
 * 나중에 서버가 블록을 추가해도 옛 앱이 깨지지 않는다.
 *
 * `text` 안에는 `**굵게**` 와 `[글자](주소)` 가 그대로 남아 온다.
 */
sealed interface StoryBlock {
    data class Heading(val level: Int, val text: String) : StoryBlock
    data class Paragraph(val text: String) : StoryBlock
    data class BulletList(val items: List<String>, val ordered: Boolean) : StoryBlock
    data class Quote(val text: String) : StoryBlock
    data class Image(val url: String, val alt: String?) : StoryBlock
    data object Divider : StoryBlock

    /**
     * 축제·관광지 카드. 제목·날짜·사진은 글에 저장돼 있지 않고
     * 서버가 응답을 만들 때 DB 에서 지금 값을 읽어 채운다 — 1년 전 글도 카드는 최신이다.
     * 연결한 축제가 사라졌으면 서버가 **그 블록만 빼고** 내려준다.
     */
    data class FestivalCard(
        val contentId: Long,
        val title: String,
        val region: String?,
        val thumbUrl: String?,
        val period: ClosedRange<LocalDate>?,
    ) : StoryBlock

    data class PlaceCard(
        val contentId: Long,
        val title: String,
        val region: String?,
        val thumbUrl: String?,
    ) : StoryBlock
}
