package com.mosstis.kofest.feature.festival.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.HomeFeed
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.OfflineBand
import java.time.LocalDate

@Composable
fun HomeScreen(
    uiState: HomeContract.State,
    onAction: (HomeContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(HomeContract.Action.SelectLanguage(it)) },
            onSearch = { /* TODO(search): 검색 화면은 아직 기획서에 정의가 없다 */ },
        )

        if (uiState.isStale) {
            OfflineBand(message = s.error.offline.fill("time" to "—"))
        }

        val feed = uiState.feed
        when {
            uiState.showSkeleton -> HomeSkeleton(Modifier.fillMaxSize())

            uiState.hasFatalError || feed == null -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = s.action.retry,
                onAction = { onAction(HomeContract.Action.Retry) },
            )

            else -> HomeContent(
                feed = feed,
                uiState = uiState,
                today = today,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun HomeContent(
    feed: HomeFeed,
    uiState: HomeContract.State,
    today: LocalDate,
    onAction: (HomeContract.Action) -> Unit,
) {
    val s = strings()
    val bannerState = rememberLazyListState()
    val selectedBanner by remember {
        derivedStateOf { bannerState.firstVisibleItemIndex }
    }

    // 언어를 바꾸면 데이터가 통째로 갈린다(항목 key 도 전부 달라진다).
    // 언어가 아니라 **데이터가 실제로 바뀐 시점**에 되돌려야 엉뚱한 장에 걸리지 않는다.
    LaunchedEffect(feed.banners) { bannerState.scrollToItem(0) }

    // 스크롤이 멈춘 뒤의 장만 노출로 센다. 미는 도중 스쳐 간 장까지 세면 노출 수가 부풀려진다.
    LaunchedEffect(bannerState, feed.banners) {
        snapshotFlow { bannerState.isScrollInProgress to bannerState.firstVisibleItemIndex }
            .collect { (scrolling, index) ->
                if (!scrolling && index in feed.banners.indices) {
                    onAction(HomeContract.Action.BannerShown)
                }
            }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (feed.banners.isNotEmpty()) {
            item(key = "banners") {
                BoxWithConstraints {
                    // 오른쪽 끝에서 다음 장이 30dp 보인다. 스냅하면 다시 22dp 선에 붙는다.
                    val bannerWidth = maxWidth - KoFestDimens.ScreenMargin - KoFestDimens.Peek
                    LazyRow(
                        state = bannerState,
                        flingBehavior = rememberSnapFlingBehavior(bannerState),
                        contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement
                            .spacedBy(KoFestDimens.BannerGap),
                    ) {
                        items(feed.banners, key = { it.id }) { banner ->
                            BannerCard(
                                banner = banner,
                                onClick = { onAction(HomeContract.Action.OpenBanner(banner)) },
                                modifier = Modifier.width(bannerWidth),
                            )
                        }
                    }
                }
            }
            item(key = "dots") {
                Spacer(Modifier.height(14.dp))
                BannerDots(count = feed.banners.size, selected = selectedBanner)
            }
        }

        // 진행중 0건이면 섹션 자체를 숨긴다.
        if (feed.ongoing.isNotEmpty()) {
            item(key = "ongoing") {
                Spacer(Modifier.height(38.dp))
                SectionHeader(
                    title = s.home.ongoingTitle,
                    sub = s.home.ongoingSub,
                    seeAllCount = feed.ongoing.size,
                    onSeeAll = { onAction(HomeContract.Action.OpenOngoing) },
                )
                FestivalCardRow(
                    festivals = feed.ongoing,
                    today = today,
                    language = uiState.language,
                    showStateMark = true,
                    onClick = { onAction(HomeContract.Action.OpenFestival(it)) },
                )
            }
        }

        // "지금 떠나기 좋은 곳" — 테마. 0건이면 섹션을 통째로 뺀다 (개발문서2 6장).
        if (uiState.themes.isNotEmpty()) {
            item(key = "themes") {
                Spacer(Modifier.height(38.dp))
                SectionHeader(
                    title = s.theme.title,
                    sub = s.theme.sub,
                    seeAllCount = null,
                    onSeeAll = {},
                )
                ThemeCarousel(
                    themes = uiState.themes,
                    expandedThemeId = uiState.expandedThemeId,
                    onToggle = { onAction(HomeContract.Action.ToggleTheme(it)) },
                    onItem = { kind, contentId ->
                        onAction(HomeContract.Action.OpenThemeItem(kind, contentId))
                    },
                )
            }
        }

        // "가볼 만한 곳" — 관광지 8건. 이번 주말 자리를 대체한 섹션이다 (기획서 02).
        if (uiState.picks.isNotEmpty()) {
            item(key = "picks") {
                Spacer(Modifier.height(38.dp))
                SectionHeader(
                    title = s.home.picksTitle,
                    sub = s.home.picksSub,
                    seeAllCount = null,
                    onSeeAll = {},
                )
                PlaceCardRow(
                    places = uiState.picks,
                    onClick = { onAction(HomeContract.Action.OpenPlace(it)) },
                )
            }
        }

        item(key = "regions") {
            Spacer(Modifier.height(38.dp))
            SectionHeader(
                title = s.home.regionTitle,
                sub = s.home.regionSub,
                seeAllCount = null,
                onSeeAll = {},
            )
            RegionGrid(
                groups = uiState.regionGroups,
                ungrouped = uiState.ungroupedRegions,
                totalCount = feed.totalCount,
                onRegion = { onAction(HomeContract.Action.OpenRegion(it)) },
                onAll = { onAction(HomeContract.Action.OpenAllFestivals) },
            )
        }

        // 매거진 최신 3건. `storyCount` 가 0 이면 ViewModel 이 아예 부르지 않는다.
        if (uiState.latestStories.isNotEmpty()) {
            item(key = "stories") {
                Spacer(Modifier.height(38.dp))
                SectionHeader(
                    title = s.story.title,
                    sub = s.story.sub,
                    seeAllCount = null,
                    onSeeAll = { onAction(HomeContract.Action.OpenMagazine) },
                )
                StoryPreviewList(
                    stories = uiState.latestStories,
                    onClick = { onAction(HomeContract.Action.OpenStory(it)) },
                )
            }
        }

        item(key = "language-notice") {
            Spacer(Modifier.height(40.dp))
            LanguageNotice(
                language = uiState.language,
                counts = feed.counts,
                onSwitch = { onAction(HomeContract.Action.SelectLanguage(it)) },
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun FestivalCardRow(
    festivals: List<com.mosstis.kofest.domain.festival.model.Festival>,
    today: LocalDate,
    language: com.mosstis.kofest.core.common.AppLanguage,
    showStateMark: Boolean,
    onClick: (Long) -> Unit,
) {
    val rowState = rememberLazyListState()
    LaunchedEffect(festivals) { rowState.scrollToItem(0) }

    LazyRow(
        state = rowState,
        contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement
            .spacedBy(KoFestDimens.CardGap),
    ) {
        items(festivals, key = { it.contentId }) { festival ->
            FestivalCard(
                festival = festival,
                today = today,
                language = language,
                showStateMark = showStateMark,
                onClick = { onClick(festival.contentId) },
            )
        }
    }
}

/** 스피너를 쓰지 않는다. 실제 배치 그대로 회색 뼈대를 놓아야 화면이 흔들리지 않는다. */
@Composable
private fun HomeSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = KoFestDimens.Peek),
            height = KoFestDimens.BannerHeight,
        )
        Spacer(Modifier.height(38.dp))
        SkeletonBlock(modifier = Modifier.width(140.dp), height = 19.dp)
        Spacer(Modifier.height(16.dp))
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement
                .spacedBy(KoFestDimens.CardGap),
        ) {
            repeat(3) {
                Column(Modifier.width(KoFestDimens.CardWidth)) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 5f),
                        height = 210.dp,
                    )
                    Spacer(Modifier.height(12.dp))
                    SkeletonBlock(Modifier.fillMaxWidth(), height = 14.dp)
                    Spacer(Modifier.height(6.dp))
                    SkeletonBlock(Modifier.width(90.dp))
                }
            }
        }
    }
}
