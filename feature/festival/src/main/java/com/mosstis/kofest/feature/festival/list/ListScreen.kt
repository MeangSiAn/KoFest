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

    val regionCode = uiState.filter.regionCode
    val title = if (regionCode == null) {
        s.list.title
    } else {
        s.list.titleRegion.fill("region" to (regionNames[regionCode] ?: regionCode))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        ListHeader(
            title = title,
            onSearch = { /* TODO(search): 검색 화면은 아직 기획서에 정의가 없다 */ },
        )

        if (uiState.isStale) {
            OfflineBand(message = s.error.offline.fill("time" to "—"))
        }

        FilterBar(
            regionLabel = regionNames[regionCode] ?: s.filter.regionAll,
            // 지역은 '전국'이라도 항상 값이 있는 필터라 켜진 상태로 보인다.
            regionSelected = true,
            periodLabel = s.filter.thisMonth,
            periodSelected = uiState.filter.from != null,
            ongoingSelected = uiState.filter.ongoingOnly,
            hasImageSelected = uiState.filter.hasImageOnly,
            // TODO(filter): 지역·시기는 2단계 선택 시트가 필요하다. 지금은 전국↔최근 선택 토글로 둔다.
            onRegion = { onAction(ListContract.Action.SelectRegion(null)) },
            onPeriod = { onAction(ListContract.Action.ToggleThisMonth) },
            onOngoing = { onAction(ListContract.Action.ToggleOngoing) },
            onHasImage = { onAction(ListContract.Action.ToggleHasImage) },
        )

        when {
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
    val regionCode = uiState.filter.regionCode
    val regionName = regionCode?.let { s.region[it] ?: it }

    val title = if (regionName != null) {
        s.empty.noResultRegion.fill("region" to regionName)
    } else {
        s.empty.noResult
    }

    val switchesLanguage = uiState.language == AppLanguage.EN && uiState.counts.ko > uiState.counts.en

    EmptyState(
        title = title,
        // 영어에서 0건이면 한국어로 바꾸면 몇 건인지 알려준다.
        body = if (switchesLanguage) {
            s.language.moreHere.fill("count" to uiState.counts.ko)
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

private const val LOAD_MORE_THRESHOLD = 3
private const val SKELETON_ROWS = 6
