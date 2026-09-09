package com.mosstis.kofest.feature.festival.intro

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.GetLanguageCountsUseCase
import com.mosstis.kofest.domain.festival.usecase.ObserveLanguageSelectedUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val getLanguageCounts: GetLanguageCountsUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val observeLanguageSelected: ObserveLanguageSelectedUseCase,
    private val repository: FestivalRepository,
) : BaseViewModel<IntroContract.State, IntroContract.Action, IntroContract.Effect>(
    initialState = IntroContract.State(),
) {

    init {
        viewModelScope.launch { runSplash() }
    }

    override fun handleAction(action: IntroContract.Action) {
        when (action) {
            is IntroContract.Action.SelectLanguage ->
                updateState { copy(selected = action.language) }

            IntroContract.Action.Start -> viewModelScope.launch {
                setLanguage(currentState.selected)
                sendEffect(IntroContract.Effect.NavigateToHome)
            }
        }
    }

    /**
     * 최소 [MIN_SPLASH_MS] 는 보여주고, [MAX_SPLASH_MS] 를 넘기면 기다리지 않는다.
     * 응답이 빨라도 깜빡이지 않게, 느려도 붙잡아두지 않게.
     *
     * 진행률 막대를 두지 않는다 — 얼마나 걸릴지 모르면서 그리면 거짓말이 된다.
     */
    private suspend fun runSplash() {
        // 기기 언어를 기본 선택으로 채운다. 저장된 값이 있으면 그 값이 온다.
        val defaultLanguage = repository.language.first()
        updateState { copy(selected = defaultLanguage) }

        val alreadySelected = observeLanguageSelected().first()

        val elapsed = measureTimeMillis {
            // 건수 조회가 곧 'API 첫 응답'이다. 실패해도 인트로를 막지 않는다 —
            // 숫자 없이 언어 이름만 보여주면 된다.
            val counts = withTimeoutOrNull(MAX_SPLASH_MS) {
                runCatching { getLanguageCounts() }
                    .onFailure { android.util.Log.w(TAG, "언어별 건수 조회 실패", it) }
                    .getOrNull()
            }
            updateState { copy(counts = counts) }
        }

        val remaining = MIN_SPLASH_MS - elapsed
        if (remaining > 0) kotlinx.coroutines.delay(remaining)

        if (alreadySelected) {
            sendEffect(IntroContract.Effect.NavigateToHome)
        } else {
            updateState { copy(phase = IntroContract.Phase.LANGUAGE) }
        }
    }

    private companion object {
        const val MIN_SPLASH_MS = 800L
        const val MAX_SPLASH_MS = 3_000L
        const val TAG = "IntroViewModel"
    }
}
