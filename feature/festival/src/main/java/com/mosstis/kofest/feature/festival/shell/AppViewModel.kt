package com.mosstis.kofest.feature.festival.shell

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.analytics.AppEvent
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val setLanguage: SetLanguageUseCase,
    tracker: EventTracker,
    repository: FestivalRepository,
) : BaseViewModel<AppContract.State, AppContract.Action, AppContract.Effect>(
    initialState = AppContract.State(),
) {

    init {
        tracker.track(AppEvent.APP_OPEN)

        repository.language
            .onEach { language -> updateState { copy(language = language) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: AppContract.Action) {
        when (action) {
            is AppContract.Action.SelectLanguage ->
                viewModelScope.launch { setLanguage(action.language) }
        }
    }
}
