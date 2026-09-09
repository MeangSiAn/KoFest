package com.mosstis.kofest.feature.festival.home

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Banner
import com.mosstis.kofest.domain.festival.model.HomeFeed
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
    ) : UiState {
        val showSkeleton: Boolean get() = isLoading && feed == null
    }

    sealed interface Action : UiAction {
        data object ScreenStarted : Action
        data object Retry : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenFestival(val contentId: Long) : Action
        data class OpenBanner(val banner: Banner) : Action
        data class OpenRegion(val regionCode: String) : Action
        data object OpenAllFestivals : Action
        data object OpenOngoing : Action
        data object OpenWeekend : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
        data class OpenUrl(val url: String) : Effect
        data class NavigateToList(
            val regionCode: String? = null,
            val ongoingOnly: Boolean = false,
        ) : Effect
    }
}
