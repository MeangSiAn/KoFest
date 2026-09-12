package com.mosstis.kofest.feature.festival.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.RegionGroup
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.OfflineBand
import java.time.LocalDate

@Composable
fun ListScreen(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()
    val regionNames = s.region
    val listState = rememberLazyListState()

    // 끝에 가까워지면 다음 페이지. 커서 페이징이라 offset 이 어긋날 일이 없다.
    LaunchedEffect(listState, uiState.hasMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (uiState.hasMore && total > 0 && lastVisible >= total - LOAD_MORE_THRESHOLD) {
                    onAction(ListContract.Action.LoadMore)
                }
            }
    }

    // 필터를 걸어 그 달로 옮겨가야 하면, 데이터가 들어온 뒤 그 섹션 머리로 스크롤한다.
    LaunchedEffect(uiState.scrollToMonth, uiState.sections) {
        val target = uiState.scrollToMonth ?: return@LaunchedEffect
        var index = 0
        for (section in uiState.sections) {
            if (section.month == target) {
                listState.scrollToItem(index)
                break
            }
            index += 1 + section.festivals.size   // 월 헤더 + 그 달의 행들
        }
        onAction(ListContract.Action.ScrollHandled)
    }

    val regionLabel = regionLabelOf(uiState.filter.regionCodes)
    val title = if (regionLabel == null) {
        s.list.title
    } else {
        s.list.titleRegion.fill("region" to regionLabel)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        ListHeader(
            title = title,
            mode = uiState.mode,
            onSelectMode = { onAction(ListContract.Action.SelectMode(it)) },
            onSearch = { /* TODO(search): 검색 화면은 아직 기획서에 정의가 없다 */ },
            showModeSegment = uiState.tab == ListContract.Tab.FESTIVAL,
        )

        // 축제와 관광은 다른 테이블이라 정렬 축도 다르다 (기획서 03).
        ListTabs(
            tab = uiState.tab,
            festivalCount = uiState.counts.of(uiState.language).festival.takeIf { it > 0 },
            placeCount = uiState.placeTotal,
            onSelect = { onAction(ListContract.Action.SelectTab(it)) },
        )

        if (uiState.isStale) {
            OfflineBand(message = s.error.offline.fill("time" to "—"))
        }

        FilterBar(
            regionLabel = regionLabel ?: s.filter.regionAll,
            // 지역은 '전국'이라도 항상 값이 있는 필터라 켜진 상태로 보인다.
            regionSelected = true,
            periodLabel = s.filter.thisMonth,
            periodSelected = uiState.filter.from != null,
            ongoingSelected = uiState.filter.ongoingOnly,
            hasImageSelected = uiState.filter.hasImageOnly,
            // TODO(filter): 지역·시기는 2단계 선택 시트가 필요하다. 지금은 전국↔최근 선택 토글로 둔다.
            onRegion = { onAction(ListContract.Action.SelectRegion(emptyList())) },
            onPeriod = { onAction(ListContract.Action.ToggleThisMonth) },
            onOngoing = { onAction(ListContract.Action.ToggleOngoing) },
            onHasImage = { onAction(ListContract.Action.ToggleHasImage) },
            // 관광 탭 목업(기획서 03)에는 "사진 있는 것" 칩만 있다. 달력 보기는 목록과 필터를 그대로 공유한다.
            // TODO(place): 관광 유형 필터(12/14/28/38/39) 선택 시트. `Action.SelectPlaceType` 은 이미 있다.
            showPeriodFilters = uiState.tab == ListContract.Tab.FESTIVAL,
        )

        if (uiState.tab == ListContract.Tab.PLACE) {
            PlaceList(uiState = uiState, onAction = onAction)
            return@Column
        }

        when {
            uiState.mode == ListContract.Mode.CALENDAR ->
                CalendarView(uiState = uiState, onAction = onAction, today = today)

            uiState.showSkeleton -> ListSkeleton()

            uiState.hasFatalError -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = s.action.retry,
                onAction = { onAction(ListContract.Action.Retry) },
            )

            uiState.isEmpty -> ListEmpty(uiState = uiState, onAction = onAction)

            else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                uiState.sections.forEach { section ->
                    stickyHeader(key = "month-${section.month}") {
                        MonthHeader(
                            month = section.month,
                            count = section.festivals.size,
                            language = uiState.language,
                            currentYear = today.year,
                        )
                    }
                    items(
                        items = section.festivals,
                        key = { it.contentId },
                    ) { festival ->
                        FestivalRow(
                            festival = festival,
                            today = today,
                            language = uiState.language,
                            onClick = { onAction(ListContract.Action.OpenFestival(festival.contentId)) },
                        )
                    }
                }

                if (uiState.isLoadingMore) {
                    item(key = "loading-more") { FestivalRowSkeleton() }
                }

                item(key = "bottom-spacer") { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/**
 * 빈 결과. 어떤 필터 때문에 0건인지 말하고 버튼은 하나만 둔다.
 * 영어에서 0건이면 **한국어로 바꾸면 몇 건인지** 알려준다.
 */
@Composable
private fun ListEmpty(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()
    val regionName = regionLabelOf(uiState.filter.regionCodes)

    val title = if (regionName != null) {
        s.empty.noResultRegion.fill("region" to regionName)
    } else {
        s.empty.noResult
    }

    val switchesLanguage = uiState.language == AppLanguage.EN && uiState.counts.ko.festival > uiState.counts.en.festival

    EmptyState(
        title = title,
        // 영어에서 0건이면 한국어로 바꾸면 몇 건인지 알려준다.
        body = if (switchesLanguage) {
            s.language.moreHere.fill("count" to uiState.counts.ko.festival)
        } else {
            s.empty.noResultHint
        },
        actionLabel = if (switchesLanguage) s.language.switch else s.action.clearFilters,
        onAction = {
            if (switchesLanguage) {
                onAction(ListContract.Action.SelectLanguage(AppLanguage.KO))
            } else {
                onAction(ListContract.Action.ClearFilters)
            }
        },
    )
}

@Composable
private fun ListSkeleton() {
    Column {
        LoadingMonthHeader()
        repeat(SKELETON_ROWS) { FestivalRowSkeleton() }
    }
}

/**
 * 관광 탭. 축제의 월 헤더 자리에 시도 헤더가 온다.
 *
 * `GET /places` 는 서버에 아직 없다 — 그때는 오류가 아니라 "준비 중"으로 보여준다.
 * 빈 목록을 그냥 두면 고장난 것처럼 보인다 (개발문서2 6장).
 */
@Composable
private fun PlaceList(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.hasMorePlaces) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (uiState.hasMorePlaces && total > 0 && lastVisible >= total - LOAD_MORE_THRESHOLD) {
                    onAction(ListContract.Action.LoadMorePlaces)
                }
            }
    }

    when {
        uiState.isLoadingPlaces && uiState.placeSections.isEmpty() -> ListSkeleton()

        uiState.placesNotReady -> EmptyState(
            title = s.place.comingSoonRegion,
            body = s.place.sub,
            actionLabel = null,
            onAction = {},
        )

        uiState.placesFailed -> EmptyState(
            title = s.error.loadFailed,
            body = s.error.loadFailedHint,
            actionLabel = s.action.retry,
            onAction = { onAction(ListContract.Action.RetryPlaces) },
        )

        uiState.placesEmpty -> EmptyState(
            title = s.place.noResult,
            body = s.place.comingSoonRegion,
            actionLabel = null,
            onAction = {},
        )

        else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            uiState.placeSections.forEach { section ->
                stickyHeader(key = "place-region-${section.region}") {
                    PlaceRegionHeader(region = section.region, count = section.places.size)
                }
                items(section.places, key = { it.contentId }) { place ->
                    PlaceRow(
                        place = place,
                        onClick = { onAction(ListContract.Action.OpenPlace(place.contentId)) },
                    )
                }
            }
            item(key = "place-bottom") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/**
 * 지역 이름. 광역권(코드 여럿)이면 광역권 이름을, 시도 하나면 시도 이름을 쓴다.
 * 서버가 준 이름(`"전남광주통합특별시"`)을 그대로 제목에 올리지 않기 위한 것이기도 하다.
 */
@Composable
private fun regionLabelOf(codes: List<String>): String? {
    if (codes.isEmpty()) return null
    val s = strings()
    val group = RegionGroup.of(codes.first())
    // 그 광역권의 코드를 다 들고 있을 때만 광역권 이름이다. 일부만 골랐으면 시도 이름이 맞다.
    if (group != null && codes.size > 1 && group.codes.containsAll(codes)) {
        return with(s.area) {
            when (group) {
                RegionGroup.SEOUL -> seoul
                RegionGroup.GYEONGGI_INCHEON -> gyeonggi
                RegionGroup.GANGWON -> gangwon
                RegionGroup.CHUNGCHEONG -> chungcheong
                RegionGroup.JEOLLA -> jeolla
                RegionGroup.GYEONGSANG -> gyeongsang
                RegionGroup.JEJU -> jeju
                RegionGroup.BUSAN_ULSAN -> busan
            }
        }
    }
    val code = codes.first()
    return s.region[code] ?: code
}

private const val LOAD_MORE_THRESHOLD = 3
private const val SKELETON_ROWS = 6
