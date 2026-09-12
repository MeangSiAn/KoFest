package com.mosstis.kofest.feature.festival.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.analytics.AppEvent
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.domain.festival.usecase.GetFestivalsUseCase
import com.mosstis.kofest.domain.festival.usecase.GetPlacesUseCase
import com.mosstis.kofest.domain.festival.usecase.GetMonthFestivalsUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 목록 화면 — 행 목록과 달력이 같은 필터를 공유한다.
 *
 * 달력은 예전에 별도 탭이었다. 기획서 05 가 목록 안의 전환으로 옮기면서
 * 두 보기의 필터가 하나여야 해서 ViewModel 도 하나로 합쳤다.
 * 지역을 부산으로 걸어둔 채 달력으로 바꾸면 부산 축제만 찍힌다.
 */
@HiltViewModel
class ListViewModel @Inject constructor(
    private val getFestivals: GetFestivalsUseCase,
    private val getMonthFestivals: GetMonthFestivalsUseCase,
    private val getPlaces: GetPlacesUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val tracker: EventTracker,
    repository: FestivalRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ListContract.State, ListContract.Action, ListContract.Effect>(
    initialState = ListContract.State(
        filter = FestivalFilter(
            // 홈에서 광역권을 누르면 시도코드가 콤마로 여럿 온다 (경기·인천 = "41,28")
            regionCodes = savedStateHandle.get<String>(ARG_REGION)
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty(),
            ongoingOnly = savedStateHandle.get<String>(ARG_ONGOING)?.toBooleanStrictOrNull() ?: false,
        ),
    ),
) {

    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        tracker.track(AppEvent.VIEW_LIST)

        repository.language
            .onEach { language ->
                // 언어가 바뀌면 달력 캐시를 통째로 버린다. 언어별로 contentId 도 다르다.
                updateState { copy(language = language, calendar = ListContract.Calendar(month = calendar.month)) }
                reload()
                if (currentState.mode == ListContract.Mode.CALENDAR) ensureMonthsLoaded()
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ListContract.Action) {
        when (action) {
            ListContract.Action.ScreenStarted -> if (currentState.sections.isEmpty()) reload()

            ListContract.Action.Retry -> reload()

            ListContract.Action.ScrollHandled -> updateState { copy(scrollToMonth = null) }

            ListContract.Action.LoadMore -> loadMore()

            ListContract.Action.ClearFilters -> applyFilter(FestivalFilter())

            ListContract.Action.ToggleOngoing ->
                applyFilter(currentState.filter.copy(ongoingOnly = !currentState.filter.ongoingOnly))

            ListContract.Action.ToggleHasImage ->
                applyFilter(currentState.filter.copy(hasImageOnly = !currentState.filter.hasImageOnly))

            is ListContract.Action.SelectRegion -> {
                tracker.track(AppEvent.FILTER_REGION)
                applyFilter(currentState.filter.copy(regionCodes = action.regionCodes))
            }

            ListContract.Action.ToggleThisMonth -> {
                val filter = currentState.filter
                if (filter.from != null) {
                    applyFilter(filter.copy(from = null, to = null))
                } else {
                    val firstDay = LocalDate.now().withDayOfMonth(1)
                    applyFilter(
                        filter = filter.copy(from = firstDay, to = firstDay.plusMonths(1).minusDays(1)),
                        scrollToMonth = firstDay,
                    )
                }
            }

            is ListContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            is ListContract.Action.OpenFestival ->
                sendEffect(ListContract.Effect.NavigateToDetail(currentState.sectionsLanguage, action.contentId))

            is ListContract.Action.SelectMode -> {
                if (action.mode == currentState.mode) return
                updateState { copy(mode = action.mode) }
                // 달력 데이터는 열어볼 때 받는다. 목록만 보는 사람에게 매달 세 번씩 부르지 않는다.
                if (action.mode == ListContract.Mode.CALENDAR) ensureMonthsLoaded()
            }

            ListContract.Action.PrevMonth -> moveTo(currentState.calendar.month.minusMonths(1))

            ListContract.Action.NextMonth -> moveTo(currentState.calendar.month.plusMonths(1))

            is ListContract.Action.SelectDay -> updateState {
                copy(
                    calendar = calendar.copy(
                        selected = if (calendar.selected == action.date) null else action.date,
                    ),
                )
            }

            ListContract.Action.RetryMonth -> {
                updateState { copy(calendar = calendar.copy(failed = calendar.failed - calendar.month)) }
                ensureMonthsLoaded()
            }

            is ListContract.Action.SelectTab -> {
                if (action.tab == currentState.tab) return
                updateState { copy(tab = action.tab) }
                // 관광지는 열어볼 때 받는다. 축제만 보는 사람에게 미리 부르지 않는다.
                if (action.tab == ListContract.Tab.PLACE && currentState.placeSections.isEmpty()) {
                    loadPlaces()
                }
            }

            is ListContract.Action.SelectPlaceType -> {
                updateState { copy(placeFilter = placeFilter.copy(type = action.type)) }
                loadPlaces()
            }

            ListContract.Action.LoadMorePlaces -> loadMorePlaces()

            ListContract.Action.RetryPlaces -> loadPlaces()

            is ListContract.Action.OpenPlace ->
                sendEffect(ListContract.Effect.NavigateToPlace(currentState.language, action.contentId))
        }
    }

    private fun loadPlaces() {
        updateState {
            copy(
                isLoadingPlaces = true,
                placesNotReady = false,
                placesFailed = false,
                placeSections = emptyList(),
                placeCursor = null,
            )
        }
        launchCatching(onError = ::onPlacesError) {
            val language = currentState.language
            // 관광지 필터는 지역 하나만 보낸다 — 서버가 여럿을 받지 않는다.
            val filter = currentState.placeFilter.copy(
                regionCode = currentState.filter.primaryRegionCode,
            )
            val page = getPlaces(language, filter)
            updateState {
                copy(
                    placeSections = page.items.toPlaceSections(),
                    placeCursor = page.nextCursor,
                    placeTotal = page.total,
                    isLoadingPlaces = false,
                )
            }
        }
    }

    private fun loadMorePlaces() {
        val cursor = currentState.placeCursor ?: return
        if (currentState.isLoadingPlaces) return

        launchCatching(
            onError = { throwable -> android.util.Log.w(TAG, "관광지 다음 페이지 실패", throwable) },
        ) {
            val filter = currentState.placeFilter.copy(
                regionCode = currentState.filter.primaryRegionCode,
            )
            val page = getPlaces(currentState.language, filter, cursor = cursor)
            updateState {
                val merged = placeSections.flatMap { it.places } + page.items
                copy(placeSections = merged.toPlaceSections(), placeCursor = page.nextCursor)
            }
        }
    }

    private fun onPlacesError(throwable: Throwable) {
        // 서버에 아직 없는 것과 진짜 실패를 나눈다.
        val notReady = throwable is FestivalDataException.NotReady
        updateState {
            copy(isLoadingPlaces = false, placesNotReady = notReady, placesFailed = !notReady)
        }
        android.util.Log.w(TAG, if (notReady) "관광지 API 가 아직 없다" else "관광지 조회 실패", throwable)
    }

    /** 축제의 월 헤더 자리에 시도 헤더가 온다. 정렬은 서버가 준 순서를 유지한다 */
    private fun List<Place>.toPlaceSections(): List<ListContract.PlaceSection> =
        groupBy { it.region.orEmpty() }
            .map { (region, items) -> ListContract.PlaceSection(region, items) }

    private fun applyFilter(filter: FestivalFilter, scrollToMonth: LocalDate? = null) {
        // 필터가 달라지면 달력에 찍힌 것도 달라진다. 받아둔 달을 버리고 다시 받는다.
        updateState {
            copy(filter = filter, calendar = ListContract.Calendar(month = calendar.month, selected = calendar.selected))
        }
        reload(scrollToMonth)
        if (currentState.mode == ListContract.Mode.CALENDAR) ensureMonthsLoaded()
    }

    private fun reload(scrollToMonth: LocalDate? = null) {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        updateState {
            copy(isLoadingMore = false, isLoading = true, hasFatalError = false, scrollToMonth = null)
        }

        loadJob = launchCatching(onError = ::onLoadError) {
            val language = currentState.language
            val filter = currentState.filter

            var page = getFestivals(language, filter)
            var items = page.items
            // 옮겨갈 달이 첫 페이지에 없으면 나올 때까지 이어 받는다.
            // 그 달에 걸치기만 하는 상설 행사가 앞을 채워 두세 페이지 뒤에 있을 수 있다.
            var extraPages = 0
            while (
                scrollToMonth != null &&
                page.nextCursor != null &&
                extraPages < MAX_PAGES_TO_REACH_MONTH &&
                items.none { it.startDate.withDayOfMonth(1) == scrollToMonth }
            ) {
                page = getFestivals(language, filter, cursor = page.nextCursor)
                items = items + page.items
                extraPages++
            }

            updateState {
                copy(
                    sections = items.toMonthSections(),
                    sectionsLanguage = language,
                    counts = page.counts,
                    nextCursor = page.nextCursor,
                    isLoading = false,
                    isStale = false,
                    hasFatalError = false,
                    scrollToMonth = scrollToMonth,
                )
            }
        }
    }

    private fun loadMore() {
        val cursor = currentState.nextCursor ?: return
        if (currentState.isLoadingMore || currentState.isLoading) return

        updateState { copy(isLoadingMore = true) }
        loadMoreJob = launchCatching(
            onError = { throwable ->
                updateState { copy(isLoadingMore = false, isStale = true) }
                android.util.Log.w(TAG, "다음 페이지 조회 실패", throwable)
            },
        ) {
            val page = getFestivals(currentState.language, currentState.filter, cursor = cursor)
            updateState {
                val merged = sections.flatMap { it.festivals } + page.items
                copy(
                    sections = merged.toMonthSections(),
                    nextCursor = page.nextCursor,
                    isLoadingMore = false,
                )
            }
        }
    }

    private fun onLoadError(throwable: Throwable) {
        // 네트워크가 죽어도 이미 받은 목록은 비우지 않는다.
        updateState {
            copy(
                isLoading = false,
                isStale = sections.isNotEmpty(),
                hasFatalError = sections.isEmpty(),
            )
        }
        android.util.Log.w(TAG, "목록 조회 실패", throwable)
    }

    /** 월을 넘기면 선택은 해제된다. 목록은 그 달 전체로. */
    private fun moveTo(month: YearMonth) {
        updateState { copy(calendar = calendar.copy(month = month, selected = null)) }
        ensureMonthsLoaded()
    }

    /** 보고 있는 달 + 이전·다음 달을 받아둔다. 이미 있거나 받는 중이면 건너뛴다. */
    private fun ensureMonthsLoaded() {
        val month = currentState.calendar.month
        listOf(month, month.minusMonths(1), month.plusMonths(1)).forEach { loadMonth(it) }
    }

    private fun loadMonth(month: YearMonth) {
        val state = currentState
        if (month in state.calendar.months || month in state.calendar.loading) return
        val language = state.language
        val filter = calendarFilter()

        updateState { copy(calendar = calendar.copy(loading = calendar.loading + month)) }
        launchCatching(
            onError = { throwable ->
                updateState {
                    copy(calendar = calendar.copy(loading = calendar.loading - month, failed = calendar.failed + month))
                }
                android.util.Log.w(TAG, "달력 조회 실패 $month", throwable)
            },
        ) {
            val items = getMonthFestivals(language, month, filter)
            // 영어에서는 빈 날에 "한국어로 보면 N건" 을 알려주기 위해 한국어도 받아둔다.
            val ko = if (language == AppLanguage.EN) {
                runCatching { getMonthFestivals(AppLanguage.KO, month, filter) }.getOrNull()
            } else {
                null
            }
            updateState {
                // 그 사이 언어가 바뀌었으면 버린다.
                if (this.language != language) {
                    return@updateState copy(calendar = calendar.copy(loading = calendar.loading - month))
                }
                copy(
                    calendar = calendar.copy(
                        months = calendar.months + (month to items),
                        koMonths = if (ko != null) calendar.koMonths + (month to ko) else calendar.koMonths,
                        loading = calendar.loading - month,
                        failed = calendar.failed - month,
                    ),
                )
            }
        }
    }

    /**
     * 달력이 쓰는 필터 — 목록과 **같은 필터를 그대로** 쓴다 (기획서 05). 기간만 달력이 정하므로 뺀다.
     * ('진행중'은 오늘 기준이라 다른 달에서는 거의 빈 달력이 된다. 기획서에 정의가 없어 그대로 둔다)
     */
    private fun calendarFilter(): FestivalFilter = currentState.filter.copy(from = null, to = null)

    /** 정렬은 이미 `start_date, content_id` 로 오므로 순서를 유지한 채 월별로 묶기만 한다. */
    private fun List<Festival>.toMonthSections(): List<ListContract.MonthSection> =
        groupBy { it.startDate.withDayOfMonth(1) }
            .map { (month, items) -> ListContract.MonthSection(month, items) }
            .sortedBy { it.month }

    companion object {
        const val ARG_REGION = "region"
        const val ARG_ONGOING = "ongoing"

        /** 무한정 받지 않는다. 20개씩 5페이지면 100건 */
        private const val MAX_PAGES_TO_REACH_MONTH = 5
        private const val TAG = "ListViewModel"
    }
}
