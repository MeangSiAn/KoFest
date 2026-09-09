package com.mosstis.kofest.feature.festival.calendar

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Festival
import java.time.LocalDate
import java.time.YearMonth

object CalendarContract {

    /**
     * 월 단위 캐시. 이전·다음 달을 미리 받아두면 스와이프가 끊기지 않는다.
     * [koMonths] 는 영어 화면에서 "한국어로 보면 이날 N건" 안내를 위해 따로 받는다.
     */
    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val month: YearMonth = YearMonth.now(),
        /** 누른 날. 월을 넘기면 해제되고 목록은 그 달 전체로 */
        val selected: LocalDate? = null,
        val months: Map<YearMonth, List<Festival>> = emptyMap(),
        val koMonths: Map<YearMonth, List<Festival>> = emptyMap(),
        val loading: Set<YearMonth> = emptySet(),
        val failed: Set<YearMonth> = emptySet(),
    ) : UiState {
        val current: List<Festival>? get() = months[month]
        val isLoadingCurrent: Boolean get() = month in loading && current == null
        val failedCurrent: Boolean get() = month in failed && current == null
    }

    sealed interface Action : UiAction {
        data object PrevMonth : Action
        data object NextMonth : Action
        data class SelectDay(val date: LocalDate) : Action
        data object Retry : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenFestival(val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
    }
}
