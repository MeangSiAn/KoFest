package com.mosstis.kofest.feature.festival.home

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.analytics.AppEvent
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.model.Banner
import com.mosstis.kofest.domain.festival.model.Theme
import com.mosstis.kofest.domain.festival.model.groupByRegion
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetHomeFeedUseCase
import com.mosstis.kofest.domain.festival.usecase.GetStoriesUseCase
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
    private val getStories: GetStoriesUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val tracker: EventTracker,
    repository: FestivalRepository,
) : BaseViewModel<HomeContract.State, HomeContract.Action, HomeContract.Effect>(
    initialState = HomeContract.State(),
) {

    private var loadJob: Job? = null

    init {
        tracker.track(AppEvent.VIEW_HOME)

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
            HomeContract.Action.BannerShown -> tracker.track(AppEvent.BANNER_VIEW)

            is HomeContract.Action.OpenBanner -> when (val link = action.banner.link) {
                is Banner.Link.Url -> {
                    tracker.track(AppEvent.BANNER_CLICK)
                    sendEffect(HomeContract.Effect.OpenUrl(link.url))
                }
                is Banner.Link.FestivalDetail -> {
                    tracker.track(AppEvent.BANNER_CLICK, contentId = link.contentId)
                    sendEffect(HomeContract.Effect.NavigateToDetail(currentState.feedLanguage, link.contentId))
                }
                // 눌러도 갈 곳이 없는 배너는 클릭으로 세지 않는다. 세면 클릭률이 부풀려진다.
                null -> Unit
            }

            is HomeContract.Action.OpenRegion ->
                sendEffect(HomeContract.Effect.NavigateToList(regionCodes = action.regionCodes))

            // 별도 화면을 만들지 않는다. 카드를 누르면 그 자리에서 펼쳐진다 (기획서 08).
            is HomeContract.Action.ToggleTheme -> updateState {
                copy(expandedThemeId = if (expandedThemeId == action.themeId) null else action.themeId)
            }

            is HomeContract.Action.OpenThemeItem -> when (action.kind) {
                Theme.Kind.FESTIVAL -> sendEffect(
                    HomeContract.Effect.NavigateToDetail(currentState.feedLanguage, action.contentId),
                )
                Theme.Kind.PLACE -> sendEffect(
                    HomeContract.Effect.NavigateToPlace(currentState.feedLanguage, action.contentId),
                )
            }

            is HomeContract.Action.OpenPlace -> sendEffect(
                HomeContract.Effect.NavigateToPlace(currentState.feedLanguage, action.contentId),
            )

            is HomeContract.Action.OpenStory -> sendEffect(
                HomeContract.Effect.NavigateToStory(currentState.feedLanguage, action.slug),
            )

            HomeContract.Action.OpenMagazine -> sendEffect(HomeContract.Effect.NavigateToMagazine)

            HomeContract.Action.OpenAllFestivals ->
                sendEffect(HomeContract.Effect.NavigateToList())

            HomeContract.Action.OpenOngoing ->
                sendEffect(HomeContract.Effect.NavigateToList(ongoingOnly = true))
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
            val (groups, ungrouped) = feed.regions.groupByRegion()
            updateState {
                copy(
                    feed = feed,
                    feedLanguage = language,
                    regionGroups = groups,
                    ungroupedRegions = ungrouped,
                    isLoading = false,
                    isStale = false,
                    hasFatalError = false,
                    // 테마가 그날 바뀌면 엉뚱한 카드가 펼쳐진다. 새로 받으면 접는다.
                    expandedThemeId = null,
                )
            }

            // 매거진은 `storyCount` 가 0 이면 아예 부르지 않는다 — 섹션을 통째로 빼기 때문이다.
            if (feed.storyCount > 0) loadLatestStories(language)
        }
    }

    /**
     * 홈 아래 매거진 3건. 실패해도 홈은 그대로 둔다 —
     * 글이 없다고 축제 목록까지 빈 화면이 되면 안 된다.
     */
    private fun loadLatestStories(language: com.mosstis.kofest.core.common.AppLanguage) {
        launchCatching(
            onError = { throwable ->
                updateState { copy(latestStories = emptyList()) }
                android.util.Log.w(TAG, "홈 매거진 3건 조회 실패", throwable)
            },
        ) {
            val stories = getStories(language, limit = HOME_STORY_COUNT)
            updateState {
                // 그 사이 언어가 바뀌었으면 버린다.
                if (feedLanguage != language) this else copy(latestStories = stories.items)
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"

        /** 기획서 02 — 최신 3건 */
        const val HOME_STORY_COUNT = 3
    }
}
