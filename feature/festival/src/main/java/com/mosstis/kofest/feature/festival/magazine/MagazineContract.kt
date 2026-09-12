package com.mosstis.kofest.feature.festival.magazine

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Story

object MagazineContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val stories: List<Story> = emptyList(),
        val nextCursor: String? = null,
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        /**
         * 서버에 `/stories` 가 아직 없다. 오류가 아니라 **아직 안 열린 기능**이라
         * "다시 시도" 대신 준비 중 안내를 띄운다.
         */
        val notReady: Boolean = false,
        val hasError: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && stories.isEmpty() && !notReady && !hasError
        val hasMore: Boolean get() = nextCursor != null
    }

    sealed interface Action : UiAction {
        data object Retry : Action
        data object LoadMore : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenStory(val slug: String) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToStory(val language: AppLanguage, val slug: String) : Effect
    }
}
