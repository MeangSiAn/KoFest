package com.mosstis.kofest.feature.festival.search

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.SearchResult

object SearchContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        /** 입력창에 보이는 글자. 매 글자마다 부르지 않는다 */
        val query: String = "",
        val result: SearchResult? = null,
        val isSearching: Boolean = false,
    ) : UiState {
        /** 아직 아무것도 안 친 처음 상태 */
        val isIdle: Boolean get() = result == null && !isSearching
    }

    sealed interface Action : UiAction {
        data class QueryChanged(val query: String) : Action
        data object Clear : Action
        data object Back : Action
        data class OpenFestival(val contentId: Long) : Action
        data class OpenPlace(val contentId: Long) : Action
        /** 축제 전체 보기 → 목록 탭으로 검색어를 들고 간다 */
        data object SeeAllFestivals : Action
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToFestival(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToFestivalList(val query: String) : Effect
    }
}
