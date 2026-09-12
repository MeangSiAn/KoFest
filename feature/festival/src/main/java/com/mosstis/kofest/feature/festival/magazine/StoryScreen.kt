package com.mosstis.kofest.feature.festival.magazine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.StoryBlock
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 매거진 본문 (기획서 07).
 *
 * 서버가 문단 단위로 쪼갠 **블록 배열**을 주고 앱이 네이티브로 그린다.
 * HTML + WebView 로 하면 글꼴·여백이 앱과 따로 놀고, 축제를 누르면 앱 밖으로 샌다.
 */
@Composable
fun StoryScreen(
    uiState: StoryContract.State,
    onAction: (StoryContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()
    val content = uiState.content

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = KoFestIcons.ChevronLeft,
                contentDescription = s.action.back,
                tint = KoFestColors.Ink,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onAction(StoryContract.Action.Back) },
            )
            Text(
                text = s.story.title,
                style = KoFestTheme.type.rowTitle,
                color = KoFestColors.Muted,
            )
        }

        when {
            uiState.isLoading -> Spacer(Modifier.height(1.dp))

            uiState.notReady || uiState.hasError || content == null -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = s.action.retry,
                onAction = { onAction(StoryContract.Action.Retry) },
            )

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                item(key = "cover") {
                    content.story.coverUrl?.let { cover ->
                        FestivalImage(
                            url = cover,
                            title = content.story.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        )
                        Spacer(Modifier.height(22.dp))
                    }
                }

                item(key = "head") {
                    Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
                        Text(
                            text = content.story.title,
                            style = KoFestTheme.type.screenTitle,
                            color = KoFestColors.Ink,
                        )
                        content.story.publishedAt?.let { published ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = DATE_FORMAT.format(published.atZone(ZoneId.systemDefault())),
                                style = KoFestTheme.type.rowWhen,
                                color = KoFestColors.Muted,
                            )
                        }
                        content.story.excerpt?.let { lead ->
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = lead,
                                style = KoFestTheme.type.rowPlace,
                                color = KoFestColors.NoticeInk,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                itemsIndexed(content.blocks) { block ->
                    StoryBlockView(block = block, today = today, onAction = onAction)
                }

                item(key = "bottom") { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

/** 블록마다 key 를 만들 안정된 id 가 없어 index 를 쓴다 */
private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(
    blocks: List<StoryBlock>,
    content: @Composable (StoryBlock) -> Unit,
) {
    blocks.forEachIndexed { index, block ->
        item(key = "block-$index") { content(block) }
    }
}

@Composable
private fun StoryBlockView(
    block: StoryBlock,
    today: LocalDate,
    onAction: (StoryContract.Action) -> Unit,
) {
    val margin = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)

    when (block) {
        is StoryBlock.Heading -> Text(
            text = block.text,
            style = if (block.level == 2) KoFestTheme.type.sectionTitle else KoFestTheme.type.rowTitle,
            color = KoFestColors.Ink,
            modifier = margin.padding(top = 26.dp, bottom = 10.dp),
        )

        is StoryBlock.Paragraph -> Text(
            text = block.text.toInlineMarkup(),
            style = KoFestTheme.type.rowPlace.copy(fontSize = 14.5.sp, lineHeight = 25.sp),
            color = KoFestColors.Ink,
            modifier = margin.padding(bottom = 12.dp),
        )

        is StoryBlock.BulletList -> Column(modifier = margin.padding(bottom = 12.dp)) {
            block.items.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (block.ordered) "${index + 1}." else "·",
                        style = KoFestTheme.type.rowPlace,
                        color = KoFestColors.Muted,
                    )
                    Text(
                        text = item.toInlineMarkup(),
                        style = KoFestTheme.type.rowPlace.copy(lineHeight = 24.sp),
                        color = KoFestColors.Ink,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        is StoryBlock.Quote -> Row(
            modifier = margin.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(IntrinsicQuoteHeight)
                    .background(KoFestColors.Jaju),
            )
            Text(
                text = block.text,
                style = KoFestTheme.type.rowPlace.copy(lineHeight = 24.sp),
                color = KoFestColors.NoticeInk,
            )
        }

        is StoryBlock.Image -> {
            FestivalImage(
                url = block.url,
                title = block.alt.orEmpty(),
                contentDescription = block.alt,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .padding(vertical = 8.dp),
            )
        }

        StoryBlock.Divider -> Box(
            margin
                .padding(vertical = 22.dp)
                .fillMaxWidth()
                .height(KoFestDimens.HairlineThickness)
                .background(KoFestColors.Line),
        )

        is StoryBlock.FestivalCard -> StoryLinkCard(
            title = block.title,
            region = block.region,
            thumbUrl = block.thumbUrl,
            trailing = block.period?.let { period ->
                if (period.start == period.endInclusive) {
                    PERIOD_FORMAT.format(period.start)
                } else {
                    "${PERIOD_FORMAT.format(period.start)} – ${PERIOD_FORMAT.format(period.endInclusive)}"
                }
            },
            ended = block.period?.endInclusive?.isBefore(today) == true,
            onClick = { onAction(StoryContract.Action.OpenFestival(block.contentId)) },
        )

        is StoryBlock.PlaceCard -> StoryLinkCard(
            title = block.title,
            region = block.region,
            thumbUrl = block.thumbUrl,
            trailing = null,
            ended = false,
            onClick = { onAction(StoryContract.Action.OpenPlace(block.contentId)) },
        )
    }
}

/** 글 안에 얹힌 축제·관광지. 눌러서 앱 안의 상세로 간다 */
@Composable
private fun StoryLinkCard(
    title: String,
    region: String?,
    thumbUrl: String?,
    trailing: String?,
    ended: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(KoFestColors.NoticeBackground)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FestivalImage(
            url = thumbUrl,
            title = title,
            hanjaSize = 22.sp,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(2.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = KoFestTheme.type.rowTitle,
                color = KoFestColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(region, trailing).joinToString(" · ")
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = KoFestTheme.type.rowWhen,
                    // 이미 끝난 축제라도 글에서 빼지 않는다. 지난 것임을 색으로만 알린다.
                    color = if (ended) KoFestColors.Muted else KoFestColors.Jaju,
                )
            }
        }
    }
}

/**
 * 본문에 `**굵게**` 와 `[글자](주소)` 가 그대로 온다 (개발문서2 3장).
 *
 * 링크는 **눌러도 아무 일이 없다** — 주소를 여는 자리는 아직 정하지 않았다.
 * 처리하기 어려우면 그냥 보여줘도 읽는 데 지장이 없다는 것이 문서의 안내라,
 * 지금은 표시만 다르게 하고 마크업 기호는 지운다.
 */
private fun String.toInlineMarkup(): AnnotatedString = buildAnnotatedString {
    var rest = this@toInlineMarkup
    val pattern = Regex("""\*\*(.+?)\*\*|\[(.+?)]\((.+?)\)""")
    var index = 0
    for (match in pattern.findAll(rest)) {
        append(rest.substring(index, match.range.first))
        val bold = match.groupValues[1]
        val linkText = match.groupValues[2]
        when {
            bold.isNotEmpty() -> withStyleSpan(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(bold) }
            linkText.isNotEmpty() -> withStyleSpan(SpanStyle(color = KoFestColors.Jaju)) { append(linkText) }
        }
        index = match.range.last + 1
    }
    if (index < rest.length) append(rest.substring(index))
}

private inline fun androidx.compose.ui.text.AnnotatedString.Builder.withStyleSpan(
    style: SpanStyle,
    block: androidx.compose.ui.text.AnnotatedString.Builder.() -> Unit,
) {
    val start = length
    block()
    addStyle(style, start, length)
}

private val IntrinsicQuoteHeight = 44.dp
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val PERIOD_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
