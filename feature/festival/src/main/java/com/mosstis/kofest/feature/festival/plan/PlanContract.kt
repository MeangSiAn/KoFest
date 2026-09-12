package com.mosstis.kofest.feature.festival.plan

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.model.TravelPlanRequest
import java.time.LocalDate

object PlanContract {

    /** 입력 → (만드는 중) → 결과. 세 단계가 한 화면 안에서 바뀐다 */
    enum class Phase { INPUT, BUILDING, RESULT }

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val phase: Phase = Phase.INPUT,
        val area: TravelPlanRequest.Area = TravelPlanRequest.Area.SEOUL,
        val from: LocalDate = LocalDate.now(),
        /** 0~3박 */
        val nights: Int = 0,
        val mood: TravelPlanRequest.Mood = TravelPlanRequest.Mood.FESTIVAL,
        val plan: TravelPlan? = null,
        val notReady: Boolean = false,
        val hasError: Boolean = false,
    ) : UiState {
        val request: TravelPlanRequest
            get() = TravelPlanRequest(area = area, from = from, nights = nights, mood = mood)

        /** 오늘부터 6개월 뒤까지. 과거는 못 고른다 */
        val maxDate: LocalDate get() = LocalDate.now().plusMonths(6)
        val until: LocalDate get() = from.plusDays(nights.toLong())
    }

    sealed interface Action : UiAction {
        data class SelectArea(val area: TravelPlanRequest.Area) : Action
        data class SelectNights(val nights: Int) : Action
        data class SelectDate(val date: LocalDate) : Action
        data class SelectMood(val mood: TravelPlanRequest.Mood) : Action

        /** 칩을 누를 때마다 만들지 않는다. 이 버튼을 눌러야 부른다 */
        data object Build : Action
        data object BackToInput : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenStop(val kind: TravelPlan.Kind, val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToFestival(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
    }
}
