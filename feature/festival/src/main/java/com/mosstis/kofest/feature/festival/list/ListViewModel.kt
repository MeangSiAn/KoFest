package com.mosstis.kofest.feature.festival.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetFestivalsUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getFestivals: GetFestivalsUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ListContract.State, ListContract.Action, ListContract.Effect>(
    initialState = ListContract.State(
        filter = FestivalFilter(
            regionCode = savedStateHandle.get<String>(ARG_REGION)?.takeIf { it.isNotBlank() },
            ongoingOnly = savedStateHandle.get<String>(ARG_ONGOING)?.toBooleanStrictOrNull() ?: false,
        ),
    ),
) {

    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        repository.language
            .onEach { language ->
                updateState { copy(language = language) }
                reload()
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

            is ListContract.Action.SelectRegion ->
                applyFilter(currentState.filter.copy(regionCode = action.regionCode))

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
        }
    }

    private fun applyFilter(filter: FestivalFilter, scrollToMonth: LocalDate? = null) {
        updateState { copy(filter = filter) }
        reload(scrollToMonth)
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
