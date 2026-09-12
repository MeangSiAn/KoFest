package com.mosstis.kofest.feature.festival.place

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.PlaceDetail

object PlaceDetailContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val contentId: Long = 0,
        val detail: PlaceDetail? = null,
        val isLoading: Boolean = true,
        val notReady: Boolean = false,
        val hasError: Boolean = false,
    ) : UiState

    sealed interface Action : UiAction {
        data object Retry : Action
        data object Back : Action
        data object OpenDirections : Action
        data object Call : Action
        data class OpenNearby(val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
        data class OpenMap(val latitude: Double, val longitude: Double, val label: String) : Effect
        data class Dial(val tel: String) : Effect
    }
}
