package com.mosstis.kofest.feature.festival.magazine

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetStoriesUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MagazineViewModel @Inject constructor(
    private val getStories: GetStoriesUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<MagazineContract.State, MagazineContract.Action, MagazineContract.Effect>(
    initialState = MagazineContract.State(),
) {

    private var loadJob: Job? = null

    init {
        repository.language
            .onEach { language ->
                // 글은 언어별로 따로 쓴 것이다. 언어가 바뀌면 목록을 통째로 다시 받는다.
                updateState { copy(language = language, stories = emptyList(), nextCursor = null) }
                load()
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MagazineContract.Action) {
        when (action) {
            MagazineContract.Action.Retry -> load()

            MagazineContract.Action.LoadMore -> loadMore()

            is MagazineContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            is MagazineContract.Action.OpenStory ->
                sendEffect(
                    MagazineContract.Effect.NavigateToStory(currentState.language, action.slug),
                )
        }
    }

    private fun load() {
        loadJob?.cancel()
        updateState { copy(isLoading = true, hasError = false, notReady = false) }

        loadJob = launchCatching(onError = ::onError) {
            val page = getStories(currentState.language)
            updateState {
                copy(
                    stories = page.items,
                    nextCursor = page.nextCursor,
                    isLoading = false,
                    hasError = false,
                    notReady = false,
                )
            }
        }
    }

    private fun loadMore() {
        val cursor = currentState.nextCursor ?: return
        if (currentState.isLoadingMore || currentState.isLoading) return

        updateState { copy(isLoadingMore = true) }
        launchCatching(
            onError = { throwable ->
                updateState { copy(isLoadingMore = false) }
                android.util.Log.w(TAG, "매거진 다음 페이지 실패", throwable)
            },
        ) {
            val page = getStories(currentState.language, cursor = cursor)
            updateState {
                copy(
                    stories = stories + page.items,
                    nextCursor = page.nextCursor,
                    isLoadingMore = false,
                )
            }
        }
    }

    private fun onError(throwable: Throwable) {
        // 서버에 아직 없는 것과 진짜 실패를 나눈다. 전자에 "다시 시도"를 주면 눌러도 아무 일이 없다.
        val notReady = throwable is FestivalDataException.NotReady
        updateState {
            copy(isLoading = false, notReady = notReady, hasError = !notReady)
        }
        android.util.Log.w(TAG, if (notReady) "매거진 API 가 아직 없다" else "매거진 조회 실패", throwable)
    }

    private companion object {
        const val TAG = "MagazineViewModel"
    }
}
