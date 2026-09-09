package com.mosstis.kofest.feature.festival.my

import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.usecase.ObserveSavedFestivalsUseCase
import com.mosstis.kofest.domain.festival.usecase.SetLanguageUseCase
import com.mosstis.kofest.domain.festival.usecase.ToggleSavedFestivalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    observeSaved: ObserveSavedFestivalsUseCase,
    private val toggleSaved: ToggleSavedFestivalUseCase,
    private val setLanguage: SetLanguageUseCase,
    repository: FestivalRepository,
) : BaseViewModel<MyContract.State, MyContract.Action, MyContract.Effect>(
    initialState = MyContract.State(),
) {

    init {
        combine(repository.language, observeSaved()) { language, saved ->
            language to saved.filter { it.language == language }
        }
            .onEach { (language, items) ->
                // 저장 시점의 state 는 오래됐을 수 있다. 끝났는지는 종료일로 본다.
                val today = LocalDate.now()
                val (past, upcoming) = items.partition { it.festival.endDate.isBefore(today) }
                updateState {
                    copy(
                        language = language,
                        upcoming = upcoming.sortedWith(compareBy({ it.festival.startDate }, { it.festival.contentId })),
                        past = past.sortedWith(compareByDescending<com.mosstis.kofest.domain.festival.model.SavedFestival> { it.festival.startDate }.thenBy { it.festival.contentId }),
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MyContract.Action) {
        when (action) {
            is MyContract.Action.OpenFestival ->
                sendEffect(MyContract.Effect.NavigateToDetail(action.language, action.contentId))

            is MyContract.Action.Unsave ->
                viewModelScope.launch { toggleSaved(action.language, action.festival) }

            MyContract.Action.TogglePast -> updateState { copy(pastExpanded = !pastExpanded) }

            MyContract.Action.BrowseFestivals -> sendEffect(MyContract.Effect.NavigateToList)

            MyContract.Action.OpenLanguageDialog -> updateState { copy(showLanguageDialog = true) }

            is MyContract.Action.SelectLanguage -> {
                updateState { copy(showLanguageDialog = false) }
                viewModelScope.launch { setLanguage(action.language) }
            }

            MyContract.Action.NotifyTap -> sendEffect(MyContract.Effect.ShowNotifySoon)

            is MyContract.Action.OpenLink -> sendEffect(MyContract.Effect.OpenUrl(action.url))

            MyContract.Action.ClearCacheTap -> updateState { copy(showClearCacheDialog = true) }

            MyContract.Action.ConfirmClearCache -> {
                updateState { copy(showClearCacheDialog = false) }
                sendEffect(MyContract.Effect.ClearImageCache)
            }

            MyContract.Action.DismissDialogs ->
                updateState { copy(showLanguageDialog = false, showClearCacheDialog = false) }

            is MyContract.Action.CacheSizeMeasured -> updateState { copy(cacheBytes = action.bytes) }
        }
    }
}
