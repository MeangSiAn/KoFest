package com.mosstis.kofest.feature.festival.list

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.LanguageCounts
import java.time.LocalDate

object ListContract {

    /** 월 헤더 하나와 그 아래 행들. 월 헤더는 스크롤 시 상단에 고정된다. */
    data class MonthSection(
        val month: LocalDate,
        val festivals: List<Festival>,
    )

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val filter: FestivalFilter = FestivalFilter(),
        val sections: List<MonthSection> = emptyList(),
        /** [sections] 가 어느 언어의 데이터인지 */
        val sectionsLanguage: AppLanguage = AppLanguage.KO,
        val counts: LanguageCounts = LanguageCounts(ko = 0, en = 0),
        val nextCursor: String? = null,
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isStale: Boolean = false,
        val hasFatalError: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && sections.isEmpty() && !hasFatalError
        val showSkeleton: Boolean get() = isLoading && sections.isEmpty()
        val hasMore: Boolean get() = nextCursor != null
    }

    sealed interface Action : UiAction {
        data object ScreenStarted : Action
        data object Retry : Action
        data object LoadMore : Action
        data object ClearFilters : Action
        data object ToggleOngoing : Action
        data object ToggleHasImage : Action
        data class SelectRegion(val regionCode: String?) : Action
        data object ToggleThisMonth : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenFestival(val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
    }
}
