package com.mosstis.kofest.feature.festival.shell

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState

object AppContract {

    /**
     * 앱 전체가 공유하는 상태는 지금 언어 하나뿐이다.
     * 테마의 서체 역할과 문구표가 이 값으로 갈린다.
     */
    data class State(
        val language: AppLanguage = AppLanguage.KO,
    ) : UiState

    sealed interface Action : UiAction {
        data class SelectLanguage(val language: AppLanguage) : Action
    }

    sealed interface Effect : UiEffect
}
