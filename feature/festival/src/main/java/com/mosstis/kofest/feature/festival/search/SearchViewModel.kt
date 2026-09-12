package com.mosstis.kofest.feature.festival.search

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val search: SearchUseCase,
    repository: FestivalRepository,
) : BaseViewModel<SearchContract.State, SearchContract.Action, SearchContract.Effect>(
    initialState = SearchContract.State(),
) {

    private var searchJob: Job? = null

    init {
        repository.language
            .onEach { language ->
                updateState { copy(language = language) }
                // 언어를 바꾸면 결과의 contentId 가 달라진다. 다시 찾는다.
                if (currentState.query.isNotBlank()) runSearch(currentState.query)
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SearchContract.Action) {
        when (action) {
            is SearchContract.Action.QueryChanged -> {
                updateState { copy(query = action.query) }
                runSearch(action.query)
            }

            SearchContract.Action.Clear -> {
                searchJob?.cancel()
                updateState { copy(query = "", result = null, isSearching = false) }
            }

            SearchContract.Action.Back -> sendEffect(SearchContract.Effect.NavigateBack)

            is SearchContract.Action.OpenFestival ->
                sendEffect(SearchContract.Effect.NavigateToFestival(currentState.language, action.contentId))

            is SearchContract.Action.OpenPlace ->
                sendEffect(SearchContract.Effect.NavigateToPlace(currentState.language, action.contentId))

            SearchContract.Action.SeeAllFestivals ->
                sendEffect(SearchContract.Effect.NavigateToFestivalList(currentState.query.trim()))
        }
    }

    /**
     * 글자를 칠 때마다 부르지 않는다. [DEBOUNCE_MS] 동안 멈추면 그때 한 번 부른다 —
     * "경복궁"을 치면 세 번이 아니라 한 번이다.
     */
    private fun runSearch(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            updateState { copy(result = null, isSearching = false) }
            return
        }

        updateState { copy(isSearching = true) }
        searchJob = launchCatching(
            onError = { throwable ->
                updateState { copy(isSearching = false) }
                android.util.Log.w(TAG, "검색 실패: $trimmed", throwable)
            },
        ) {
            delay(DEBOUNCE_MS)
            val result = search(currentState.language, trimmed)
            // 기다리는 사이 글자가 바뀌었으면 버린다.
            if (currentState.query.trim() != trimmed) return@launchCatching
            updateState { copy(result = result, isSearching = false) }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 300L

        /** 한 글자로는 결과가 너무 넓다 */
        const val MIN_QUERY_LENGTH = 2
        const val TAG = "SearchViewModel"
    }
}
