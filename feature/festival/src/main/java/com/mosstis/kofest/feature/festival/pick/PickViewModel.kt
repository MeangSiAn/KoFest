package com.mosstis.kofest.feature.festival.pick

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.LocationProvider
import com.mosstis.kofest.domain.festival.usecase.PickNearbyPlaceUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickViewModel @Inject constructor(
    private val pickNearbyPlace: PickNearbyPlaceUseCase,
    private val locationProvider: LocationProvider,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<PickContract.State, PickContract.Action, PickContract.Effect>(
    initialState = PickContract.State(),
) {

    private var drawJob: Job? = null

    init {
        repository.language
            .onEach { language -> updateState { copy(language = language) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: PickContract.Action) {
        when (action) {
            PickContract.Action.ScreenStarted -> {
                // 두 번째부터는 건너뛴다. 권한은 기기가 기억하므로 우리가 다시 물을 이유가 없다.
                if (currentState.phase == PickContract.Phase.ASK && locationProvider.hasPermission()) {
                    enterStage()
                }
            }

            PickContract.Action.RequestLocation -> sendEffect(PickContract.Effect.RequestPermission)

            is PickContract.Action.PermissionResult ->
                if (action.granted) enterStage() else updateState { copy(permissionDenied = true) }

            PickContract.Action.Draw -> draw()

            PickContract.Action.Go -> currentState.picked?.let {
                sendEffect(PickContract.Effect.NavigateToPlace(currentState.pickedLanguage, it.place.contentId))
            }

            PickContract.Action.BrowseByRegion -> sendEffect(PickContract.Effect.NavigateToList)

            is PickContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }
        }
    }

    private fun enterStage() {
        updateState { copy(phase = PickContract.Phase.STAGE, permissionDenied = false) }
        locate()
    }

    private fun locate() {
        updateState { copy(isLocating = true, message = null) }
        viewModelScope.launch {
            val point = locationProvider.current()
            updateState {
                copy(
                    isLocating = false,
                    location = point,
                    message = if (point == null) PickContract.Message.LocationFailed else message,
                )
            }
        }
    }

    private fun draw() {
        if (!currentState.canDraw) return
        val location = currentState.location
        if (location == null) {
            // 위치를 못 잡았던 경우. 뽑기 대신 위치를 다시 잡는다.
            locate()
            return
        }
        // 다 본 뒤의 누름은 처음부터다.
        val exclude = if (currentState.exhausted) emptyList() else currentState.excluded
        updateState { copy(isDrawing = true, message = null, exhausted = false, excluded = exclude) }

        drawJob?.cancel()
        drawJob = launchCatching(
            onError = { throwable ->
                Log.w(TAG, "뽑기 실패", throwable)
                updateState { copy(isDrawing = false, message = PickContract.Message.Failed) }
            },
        ) {
            val started = System.currentTimeMillis()
            val language = currentState.language
            val result = pickNearbyPlace(language, location.latitude, location.longitude, exclude)
            val place = result.place
            // 결과를 먼저 넣는다 — 화면이 도는 동안 사진을 미리 받을 수 있게. 뒤집는 건 [isDrawing] 이 풀릴 때다.
            if (place != null) {
                updateState {
                    copy(
                        picked = place,
                        pickedLanguage = language,
                        km = result.km,
                        total = result.total,
                        seen = result.seen,
                        excluded = excluded + place.place.contentId,
                        exhausted = false,
                    )
                }
            }
            // 계산은 0.1초에 끝난다. 그냥 두면 깜빡하고 끝나 아무것도 안 한 것처럼 보인다 (일정 짜기와 같은 규칙).
            val remaining = MIN_DRAW_MILLIS - (System.currentTimeMillis() - started)
            if (remaining > 0) delay(remaining)

            updateState {
                when {
                    place != null -> copy(isDrawing = false)
                    result.exhausted -> copy(
                        isDrawing = false,
                        total = result.total,
                        exhausted = true,
                        message = PickContract.Message.AllSeen(result.total),
                    )
                    else -> copy(
                        isDrawing = false,
                        message = PickContract.Message.NoneInRange(result.km),
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "PickViewModel"
        const val MIN_DRAW_MILLIS = 2_000L
    }
}
