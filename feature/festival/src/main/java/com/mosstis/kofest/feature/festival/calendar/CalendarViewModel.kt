package com.mosstis.kofest.feature.festival.calendar

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetMonthFestivalsUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthFestivals: GetMonthFestivalsUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<CalendarContract.State, CalendarContract.Action, CalendarContract.Effect>(
    initialState = CalendarContract.State(),
) {

    init {
        repository.language
            .onEach { language ->
                // 언어가 바뀌면 캐시를 통째로 버린다. 언어별로 contentId 도 다르다.
                updateState {
                    copy(language = language, months = emptyMap(), koMonths = emptyMap(), loading = emptySet(), failed = emptySet())
                }
                ensureLoaded(currentState.month)
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: CalendarContract.Action) {
        when (action) {
            CalendarContract.Action.PrevMonth -> moveTo(currentState.month.minusMonths(1))
            CalendarContract.Action.NextMonth -> moveTo(currentState.month.plusMonths(1))

            is CalendarContract.Action.SelectDay ->
                updateState { copy(selected = if (selected == action.date) null else action.date) }

            CalendarContract.Action.Retry -> {
                updateState { copy(failed = failed - month) }
                ensureLoaded(currentState.month)
            }

            is CalendarContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            is CalendarContract.Action.OpenFestival ->
                sendEffect(CalendarContract.Effect.NavigateToDetail(currentState.language, action.contentId))
        }
    }

    /** 월을 넘기면 선택은 해제된다. 목록은 그 달 전체로. */
    private fun moveTo(month: YearMonth) {
        updateState { copy(month = month, selected = null) }
        ensureLoaded(month)
    }

    /** 이 달 + 이전·다음 달을 받아둔다. 이미 있거나 받는 중이면 건너뛴다. */
    private fun ensureLoaded(month: YearMonth) {
        listOf(month, month.minusMonths(1), month.plusMonths(1)).forEach { load(it) }
    }

    private fun load(month: YearMonth) {
        val state = currentState
        if (month in state.months || month in state.loading) return
        val language = state.language

        updateState { copy(loading = loading + month) }
        launchCatching(
            onError = { throwable ->
                updateState { copy(loading = loading - month, failed = failed + month) }
                android.util.Log.w(TAG, "달력 조회 실패 $month", throwable)
            },
        ) {
            val items = getMonthFestivals(language, month)
            // 영어에서는 빈 날에 "한국어로 보면 N건" 을 알려주기 위해 한국어도 받아둔다.
            val ko = if (language == AppLanguage.EN) {
                runCatching { getMonthFestivals(AppLanguage.KO, month) }.getOrNull()
            } else {
                null
            }
            updateState {
                // 그 사이 언어가 바뀌었으면 버린다.
                if (this.language != language) return@updateState copy(loading = loading - month)
                copy(
                    months = months + (month to items),
                    koMonths = if (ko != null) koMonths + (month to ko) else koMonths,
                    loading = loading - month,
                    failed = failed - month,
                )
            }
        }
    }

    private companion object {
        const val TAG = "CalendarViewModel"
    }
}
