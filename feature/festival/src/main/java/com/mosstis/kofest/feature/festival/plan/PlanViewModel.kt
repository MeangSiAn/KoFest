package com.mosstis.kofest.feature.festival.plan

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetTravelPlanUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getTravelPlan: GetTravelPlanUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<PlanContract.State, PlanContract.Action, PlanContract.Effect>(
    initialState = PlanContract.State(),
) {

    init {
        repository.language
            .onEach { language -> updateState { copy(language = language) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: PlanContract.Action) {
        when (action) {
            is PlanContract.Action.SelectArea -> updateState { copy(area = action.area) }
            is PlanContract.Action.SelectNights -> updateState { copy(nights = action.nights) }
            is PlanContract.Action.SelectDate -> updateState { copy(from = action.date) }
            is PlanContract.Action.SelectMood -> updateState { copy(mood = action.mood) }

            PlanContract.Action.Build -> build()

            PlanContract.Action.BackToInput ->
                updateState { copy(phase = PlanContract.Phase.INPUT, hasError = false, notReady = false) }

            is PlanContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }

            is PlanContract.Action.OpenStop -> when (action.kind) {
                TravelPlan.Kind.FESTIVAL -> sendEffect(
                    PlanContract.Effect.NavigateToFestival(currentState.language, action.contentId),
                )
                TravelPlan.Kind.PLACE -> sendEffect(
                    PlanContract.Effect.NavigateToPlace(currentState.language, action.contentId),
                )
                // 맛집 데이터가 없어 자리만 비워둔 것이다. 누를 수 없다.
                TravelPlan.Kind.MEAL -> Unit
            }
        }
    }

    private fun build() {
        updateState { copy(phase = PlanContract.Phase.BUILDING, hasError = false, notReady = false) }

        launchCatching(
            onError = { throwable ->
                val notReady = throwable is FestivalDataException.NotReady
                updateState {
                    copy(phase = PlanContract.Phase.RESULT, notReady = notReady, hasError = !notReady)
                }
                android.util.Log.w(TAG, if (notReady) "일정 API 가 아직 없다" else "일정 생성 실패", throwable)
            },
        ) {
            // 계산이 0.1초에 끝난다. 그냥 두면 깜빡하고 끝나서 아무것도 안 한 것처럼 보인다 —
            // 그래서 최소 2초는 보여준다 (기획서 09).
            // `async` 를 쓰지 않는다: launch 안의 async 가 실패하면 예외가 부모로 올라가
            // try/catch 를 지나쳐 **앱이 죽는다**. 순차로 받고 남은 시간만 기다린다.
            val started = System.currentTimeMillis()
            val plan = getTravelPlan(currentState.language, currentState.request)
            val remaining = MIN_BUILD_MILLIS - (System.currentTimeMillis() - started)
            if (remaining > 0) delay(remaining)

            updateState {
                copy(
                    plan = plan,
                    phase = PlanContract.Phase.RESULT,
                    notReady = false,
                    hasError = false,
                )
            }
        }
    }

    private companion object {
        const val TAG = "PlanViewModel"

        /** 최소 노출. 결과가 빨라도 "만들었다"는 인상이 필요하다 */
        const val MIN_BUILD_MILLIS = 2_000L
    }
}
