package com.mosstis.kofest.feature.festival.home

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.model.Banner
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetHomeFeedUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeFeed: GetHomeFeedUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<HomeContract.State, HomeContract.Action, HomeContract.Effect>(
    initialState = HomeContract.State(),
) {

    private var loadJob: Job? = null

    init {
        repository.language
            .onEach { language ->
                updateState { copy(language = language) }
                load()
            }
            .launchIn(viewModelScope)

        repository.lastSyncedAt
            .onEach { instant -> updateState { copy(lastSyncedAt = instant) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeContract.Action) {
        when (action) {
            HomeContract.Action.ScreenStarted -> if (currentState.feed == null) load()

            HomeContract.Action.Retry -> load()

            is HomeContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            // 언어별로 contentId 가 다르다. 화면에 보이는 카드가 속한 데이터의 언어를 함께 보낸다.
            is HomeContract.Action.OpenFestival ->
                sendEffect(HomeContract.Effect.NavigateToDetail(currentState.feedLanguage, action.contentId))

            // linkType 이 "none" 이거나 모르는 값이면 link 가 null 이라 눌러도 아무 일 없다.
            is HomeContract.Action.OpenBanner -> when (val link = action.banner.link) {
                is Banner.Link.Url -> sendEffect(HomeContract.Effect.OpenUrl(link.url))
                is Banner.Link.FestivalDetail ->
                    sendEffect(HomeContract.Effect.NavigateToDetail(currentState.feedLanguage, link.contentId))
                null -> Unit
            }

            is HomeContract.Action.OpenRegion ->
                sendEffect(HomeContract.Effect.NavigateToList(regionCode = action.regionCode))

            HomeContract.Action.OpenAllFestivals ->
                sendEffect(HomeContract.Effect.NavigateToList())

            HomeContract.Action.OpenOngoing ->
                sendEffect(HomeContract.Effect.NavigateToList(ongoingOnly = true))

            HomeContract.Action.OpenWeekend ->
                sendEffect(HomeContract.Effect.NavigateToList())
        }
    }

    private fun load() {
        loadJob?.cancel()
        updateState { copy(isLoading = true, hasFatalError = false) }

        loadJob = launchCatching(
            onError = { throwable ->
                // 실패와 '데이터 없음'을 구분한다. 보여줄 것이 있으면 화면을 비우지 않는다.
                updateState {
                    copy(
                        isLoading = false,
                        isStale = feed != null,
                        hasFatalError = feed == null,
                    )
                }
                android.util.Log.w(TAG, "홈 피드 조회 실패", throwable)
            },
        ) {
            val language = currentState.language
            val feed = getHomeFeed(language)
            updateState {
                copy(feed = feed, feedLanguage = language, isLoading = false, isStale = false, hasFatalError = false)
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
