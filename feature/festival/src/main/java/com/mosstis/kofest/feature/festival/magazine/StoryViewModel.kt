package com.mosstis.kofest.feature.festival.magazine

import androidx.lifecycle.SavedStateHandle
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.domain.festival.usecase.GetStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val getStory: GetStoryUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<StoryContract.State, StoryContract.Action, StoryContract.Effect>(
    initialState = StoryContract.State(
        // 글도 언어별로 따로 쓴 것이라 경로에 언어를 담는다.
        language = AppLanguage.from(savedStateHandle.get<String>(ARG_LANG).orEmpty()),
        slug = savedStateHandle.get<String>(ARG_SLUG).orEmpty(),
    ),
) {

    init {
        load()
    }

    override fun handleAction(action: StoryContract.Action) {
        when (action) {
            StoryContract.Action.Retry -> load()

            StoryContract.Action.Back -> sendEffect(StoryContract.Effect.NavigateBack)

            is StoryContract.Action.OpenFestival -> sendEffect(
                StoryContract.Effect.NavigateToFestival(currentState.language, action.contentId),
            )

            is StoryContract.Action.OpenPlace -> sendEffect(
                StoryContract.Effect.NavigateToPlace(currentState.language, action.contentId),
            )
        }
    }

    private fun load() {
        updateState { copy(isLoading = true, hasError = false, notReady = false) }
        launchCatching(
            onError = { throwable ->
                val notReady = throwable is FestivalDataException.NotReady
                updateState { copy(isLoading = false, notReady = notReady, hasError = !notReady) }
                android.util.Log.w(TAG, "글 조회 실패 ${currentState.slug}", throwable)
            },
        ) {
            val content = getStory(currentState.language, currentState.slug)
            updateState { copy(content = content, isLoading = false) }
        }
    }

    companion object {
        const val ARG_LANG = "lang"
        const val ARG_SLUG = "slug"
        private const val TAG = "StoryViewModel"
    }
}
