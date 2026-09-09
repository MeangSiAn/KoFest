package com.mosstis.kofest.feature.festival.intro

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.LanguageCounts

object IntroContract {

    enum class Phase {
        /** API 첫 응답을 기다리는 동안. 브랜드를 각인시키는 자리가 아니라 빈 흰 화면을 막는 자리다 */
        SPLASH,

        /** 최초 실행 1회만 */
        LANGUAGE,
    }

    data class State(
        val phase: Phase = Phase.SPLASH,
        /** 기기 언어로 정한 기본 선택. 아무것도 안 고른 상태를 만들지 않는다 */
        val selected: AppLanguage = AppLanguage.KO,
        /**
         * 언어별 축제 수. 응답이 늦거나 실패하면 null 로 두고 **숫자 없이** 언어 이름만 보여준다.
         * "0건"으로 표시하면 앱이 비어 있다고 오해한다.
         */
        val counts: LanguageCounts? = null,
    ) : UiState

    sealed interface Action : UiAction {
        data class SelectLanguage(val language: AppLanguage) : Action

        /** 시작하기. 그냥 눌러도 기본 선택으로 진행된다 */
        data object Start : Action
    }

    sealed interface Effect : UiEffect {
        data object NavigateToHome : Effect
    }
}
