package com.mosstis.kofest.feature.festival.magazine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.home.HomeHeader
import com.mosstis.kofest.feature.festival.home.SectionHeader
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 매거진 목록 (기획서 07).
 *
 * 홈 섹션 하나로 두면 스크롤 아래에 묻힌다. 글은 축제 목록과 성격이 다른 콘텐츠라 자기 자리가 필요하다.
 */
@Composable
fun MagazineScreen(
    uiState: MagazineContract.State,
    onAction: (MagazineContract.Action) -> Unit,
    /** 상단 검색 아이콘. 검색은 탭이 아니라 검색창에서 들어간다 */
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (uiState.hasMore && total > 0 && lastVisible >= total - 2) {
                    onAction(MagazineContract.Action.LoadMore)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(MagazineContract.Action.SelectLanguage(it)) },
            onSearch = onSearch,
            showSearch = false,
        )

        when {
            uiState.isLoading -> MagazineSkeleton()

            uiState.notReady || uiState.hasError -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = s.action.retry,
                onAction = { onAction(MagazineContract.Action.Retry) },
            )

            uiState.isEmpty -> EmptyState(
                title = s.story.empty,
                body = s.story.sub,
                actionLabel = null,
                onAction = {},
            )

            else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item(key = "head") {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader(
                        title = s.story.title,
                        sub = s.story.sub,
                        seeAllCount = null,
                        onSeeAll = {},
                    )
                }
                items(uiState.stories, key = { it.id }) { story ->
                    StoryRow(
                        story = story,
                        onClick = { onAction(MagazineContract.Action.OpenStory(story.slug)) },
                    )
                }
                if (uiState.isLoadingMore) {
                    item(key = "more") { StoryRowSkeleton() }
                }
                item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/**
 * 글 한 줄. 표지가 없으면 제목 첫 글자를 자주색 면에 얹는다 — 회색 네모를 쓰지 않는다.
 */
@Composable
private fun StoryRow(story: Story, onClick: () -> Unit) {
    val s = strings()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FestivalImage(
                url = story.coverUrl,
                title = story.title,
                hanjaSize = 26.sp,
                modifier = Modifier
                    .size(width = 96.dp, height = 74.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (story.pinned) {
                    Text(
                        text = s.story.pinned,
                        style = KoFestTheme.type.badge,
                        color = KoFestColors.Jaju,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = story.title,
                    style = KoFestTheme.type.rowTitle,
                    color = KoFestColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                story.excerpt?.let { excerpt ->
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = excerpt,
                        style = KoFestTheme.type.rowPlace,
                        color = KoFestColors.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                story.publishedAt?.let { published ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = DATE_FORMAT.format(published.atZone(ZoneId.systemDefault())),
                        style = KoFestTheme.type.rowWhen,
                        color = KoFestColors.Muted,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(KoFestDimens.HairlineThickness)
                .background(KoFestColors.Line),
        )
    }
}

@Composable
private fun MagazineSkeleton() {
    Column(modifier = Modifier.padding(top = 18.dp)) {
        repeat(4) { StoryRowSkeleton() }
    }
}

@Composable
private fun StoryRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SkeletonBlock(modifier = Modifier.size(width = 96.dp, height = 74.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(15.dp))
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f).height(11.dp))
        }
    }
}

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
