package com.mosstis.kofest.feature.festival.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.base.BaseViewModel
import com.mosstis.kofest.domain.festival.usecase.GetFestivalDetailUseCase
import com.mosstis.kofest.domain.festival.usecase.IsFestivalSavedUseCase
import com.mosstis.kofest.domain.festival.usecase.ToggleSavedFestivalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getFestivalDetail: GetFestivalDetailUseCase,
    private val toggleSaved: ToggleSavedFestivalUseCase,
    isFestivalSaved: IsFestivalSavedUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DetailContract.State, DetailContract.Action, DetailContract.Effect>(
    initialState = DetailContract.State(
        language = AppLanguage.from(savedStateHandle.get<String>(ARG_LANG).orEmpty()),
    ),
) {

    /**
     * 언어별로 contentId 가 다르므로 경로에 언어를 함께 담는다.
     * 현재 앱 언어를 읽어 오면, 목록에서 들어온 뒤 언어가 바뀌었을 때 없는 축제를 조회하게 된다.
     */
    private val contentId: Long = savedStateHandle.get<String>(ARG_CONTENT_ID)?.toLongOrNull() ?: 0L

    init {
        load()

        isFestivalSaved(currentState.language, contentId)
            .onEach { saved -> updateState { copy(isSaved = saved) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: DetailContract.Action) {
        when (action) {
            DetailContract.Action.Retry -> load()

            DetailContract.Action.Back ->
                sendEffect(DetailContract.Effect.NavigateBack)

            DetailContract.Action.ToggleOverview ->
                updateState { copy(overviewExpanded = !overviewExpanded) }

            DetailContract.Action.ToggleSaved -> {
                val festival = currentState.detail?.festival ?: return
                viewModelScope.launch { toggleSaved(currentState.language, festival) }
            }

            DetailContract.Action.Share -> {
                val detail = currentState.detail ?: return
                sendEffect(
                    DetailContract.Effect.ShareFestival(
                        title = detail.festival.title,
                        text = listOf(detail.festival.title, detail.address ?: detail.festival.region)
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
                    ),
                )
            }

            DetailContract.Action.OpenDirections -> {
                val detail = currentState.detail ?: return
                val latitude = detail.latitude ?: return
                val longitude = detail.longitude ?: return
                sendEffect(
                    DetailContract.Effect.OpenMap(latitude, longitude, detail.festival.title),
                )
            }

            DetailContract.Action.Call -> {
                val tel = currentState.detail?.tel ?: return
                sendEffect(DetailContract.Effect.Dial(tel))
            }

            DetailContract.Action.OpenHomepage -> {
                val url = currentState.detail?.homepageUrl ?: return
                sendEffect(DetailContract.Effect.OpenUrl(url))
            }

            is DetailContract.Action.OpenPhotoViewer ->
                updateState { copy(viewerIndex = action.index) }

            DetailContract.Action.ClosePhotoViewer ->
                updateState { copy(viewerIndex = null) }

            is DetailContract.Action.OpenNearby ->
                sendEffect(DetailContract.Effect.NavigateToDetail(currentState.language, action.contentId))
        }
    }

    private fun load() {
        updateState { copy(isLoading = true, hasFatalError = false) }

        launchCatching(
            onError = { throwable ->
                updateState { copy(isLoading = false, hasFatalError = detail == null) }
                android.util.Log.w(TAG, "상세 조회 실패 contentId=$contentId", throwable)
            },
        ) {
            val detail = getFestivalDetail(currentState.language, contentId)
            updateState { copy(detail = detail, isLoading = false, hasFatalError = false) }
        }
    }

    companion object {
        const val ARG_LANG = "lang"
        const val ARG_CONTENT_ID = "contentId"
        private const val TAG = "DetailViewModel"
    }
}
