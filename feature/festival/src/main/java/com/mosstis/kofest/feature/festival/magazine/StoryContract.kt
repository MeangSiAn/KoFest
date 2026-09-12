package com.mosstis.kofest.feature.festival.magazine

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.StoryContent

object StoryContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val slug: String = "",
        val content: StoryContent? = null,
        val isLoading: Boolean = true,
        val notReady: Boolean = false,
        val hasError: Boolean = false,
    ) : UiState

    sealed interface Action : UiAction {
        data object Retry : Action
        data object Back : Action
        data class OpenFestival(val contentId: Long) : Action
        data class OpenPlace(val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect

        /**
         * 글에 얹힌 축제·관광지는 **앱 안의 상세로** 간다.
         * 이게 HTML 대신 블록 배열로 받는 이유다 — WebView 면 브라우저가 열려 앱을 떠난다.
         */
        data class NavigateToFestival(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
    }
}
