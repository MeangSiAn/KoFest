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
                applyFilter(
                    if (filter.from != null) {
                        filter.copy(from = null, to = null)
                    } else {
                        val today = LocalDate.now()
                        filter.copy(
                            from = today.withDayOfMonth(1),
                            to = today.withDayOfMonth(today.lengthOfMonth()),
                        )
                    },
                )
            }

            is ListContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            is ListContract.Action.OpenFestival ->
                sendEffect(ListContract.Effect.NavigateToDetail(currentState.sectionsLanguage, action.contentId))
        }
    }

    private fun applyFilter(filter: FestivalFilter) {
        updateState { copy(filter = filter) }
        reload()
    }

    private fun reload() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        updateState { copy(isLoadingMore = false, isLoading = true, hasFatalError = false) }

        loadJob = launchCatching(onError = ::onLoadError) {
            val language = currentState.language
            val page = getFestivals(language, currentState.filter)
            updateState {
                copy(
                    sections = page.items.toMonthSections(),
                    sectionsLanguage = language,
                    counts = page.counts,
                    nextCursor = page.nextCursor,
                    isLoading = false,
                    isStale = false,
                    hasFatalError = false,
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
        private const val TAG = "ListViewModel"
    }
}
