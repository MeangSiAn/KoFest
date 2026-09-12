package com.mosstis.kofest.feature.festival.place

import androidx.lifecycle.SavedStateHandle
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.domain.festival.usecase.GetPlaceDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val getPlaceDetail: GetPlaceDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PlaceDetailContract.State, PlaceDetailContract.Action, PlaceDetailContract.Effect>(
    initialState = PlaceDetailContract.State(
        language = AppLanguage.from(savedStateHandle.get<String>(ARG_LANG).orEmpty()),
        contentId = savedStateHandle.get<String>(ARG_CONTENT_ID)?.toLongOrNull() ?: 0L,
    ),
) {

    init {
        load()
    }

    override fun handleAction(action: PlaceDetailContract.Action) {
        when (action) {
            PlaceDetailContract.Action.Retry -> load()

            PlaceDetailContract.Action.Back -> sendEffect(PlaceDetailContract.Effect.NavigateBack)

            PlaceDetailContract.Action.OpenDirections -> {
                val place = currentState.detail?.place ?: return
                val lat = place.latitude ?: return
                val lng = place.longitude ?: return
                sendEffect(PlaceDetailContract.Effect.OpenMap(lat, lng, place.title))
            }

            PlaceDetailContract.Action.Call -> {
                val tel = currentState.detail?.place?.tel ?: return
                sendEffect(PlaceDetailContract.Effect.Dial(tel))
            }

            is PlaceDetailContract.Action.OpenNearby -> sendEffect(
                PlaceDetailContract.Effect.NavigateToPlace(currentState.language, action.contentId),
            )
        }
    }

    private fun load() {
        updateState { copy(isLoading = true, hasError = false, notReady = false) }
        launchCatching(
            onError = { throwable ->
                val notReady = throwable is FestivalDataException.NotReady
                updateState { copy(isLoading = false, notReady = notReady, hasError = !notReady) }
                android.util.Log.w(TAG, "관광지 조회 실패 ${currentState.contentId}", throwable)
            },
        ) {
            val detail = getPlaceDetail(currentState.language, currentState.contentId)
            updateState { copy(detail = detail, isLoading = false) }
        }
    }

    companion object {
        const val ARG_LANG = "lang"
        const val ARG_CONTENT_ID = "contentId"
        private const val TAG = "PlaceDetailViewModel"
    }
}
