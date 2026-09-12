package com.mosstis.kofest.feature.festival.home

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Banner
import com.mosstis.kofest.domain.festival.model.HomeFeed
import com.mosstis.kofest.domain.festival.model.RegionBucket
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.domain.festival.model.Theme
import java.time.Instant

object HomeContract {

    /**
     * [feed] 와 [isLoading] 을 분리해서 갖는다.
     *
     * 네트워크가 실패해도 **목록을 비우지 않는다** — 마지막으로 받은 [feed] 를 그대로 두고
     * [isStale] 로 상단에 얇은 띠만 띄운다. 외부가 죽을 때 앱까지 같이 죽으면 안 된다.
     */
    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val feed: HomeFeed? = null,
        /** [feed] 가 어느 언어의 데이터인지. 언어를 바꾸면 새 데이터가 올 때까지 [language] 와 다르다 */
        val feedLanguage: AppLanguage = AppLanguage.KO,
        val isLoading: Boolean = true,
        /** 갱신에 실패했지만 보여줄 데이터는 있는 상태 */
        val isStale: Boolean = false,
        val lastSyncedAt: Instant? = null,
        /** 보여줄 데이터가 하나도 없는 실패 */
        val hasFatalError: Boolean = false,
        /** 시도 16개를 광역권 8개로 묶은 것 (기획서 02) */
        /** 홈 아래 매거진 최신 3건. `storyCount` 가 0 이면 부르지 않는다 */
        val latestStories: List<Story> = emptyList(),
        /** 펼쳐 놓은 테마 카드. 홈을 떠났다 오면 접힌다 */
        val expandedThemeId: Long? = null,
    ) : UiState {
        val showSkeleton: Boolean get() = isLoading && feed == null

        val themes: List<Theme> get() = feed?.themes.orEmpty()
        val picks get() = feed?.picks.orEmpty()
    }

    sealed interface Action : UiAction {
        data object ScreenStarted : Action
        data object Retry : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenFestival(val contentId: Long) : Action
        data class OpenBanner(val banner: Banner) : Action

        /** 배너가 화면에 멈춰 섰다. 스쳐 지나간 장은 세지 않는다 */
        data object BannerShown : Action
        /** 광역권 하나. 안에 시도코드가 여럿이다 */
        data class OpenRegion(val regionCode: String) : Action

        /** 테마 카드를 눌렀다. 별도 화면 없이 **그 자리에서** 펼친다 (기획서 08) */
        data class ToggleTheme(val themeId: Long) : Action
        data class OpenThemeItem(val kind: com.mosstis.kofest.domain.festival.model.Theme.Kind, val contentId: Long) : Action
        data class OpenPlace(val contentId: Long) : Action
        data class OpenStory(val slug: String) : Action
        data object OpenMagazine : Action
        data object OpenAllFestivals : Action
        data object OpenOngoing : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
        data class OpenUrl(val url: String) : Effect
        data class NavigateToList(
            val regionCodes: List<String> = emptyList(),
            val ongoingOnly: Boolean = false,
        ) : Effect

        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToStory(val language: AppLanguage, val slug: String) : Effect
        data object NavigateToMagazine : Effect
    }
}
