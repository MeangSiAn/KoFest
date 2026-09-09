package com.mosstis.kofest.feature.festival.detail

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.FestivalPhoto

object DetailContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val detail: FestivalDetail? = null,
        val isLoading: Boolean = true,
        val isSaved: Boolean = false,
        val hasFatalError: Boolean = false,
        /** 소개글을 펼쳤는지. 기본은 4줄까지만 보인다 */
        val overviewExpanded: Boolean = false,
        /** 전체화면 사진 뷰어에서 보고 있는 사진 위치. null 이면 닫힘 */
        val viewerIndex: Int? = null,
    ) : UiState {

        /** 대표 이미지 + 갤러리. 히어로 페이저와 뷰어가 같은 목록을 쓴다 */
        val photos: List<FestivalPhoto>
            get() {
                val detail = detail ?: return emptyList()
                val hero = detail.imageUrl?.let {
                    FestivalPhoto(url = it, thumbUrl = it, caption = null)
                }
                return listOfNotNull(hero) + detail.photos
            }
    }

    sealed interface Action : UiAction {
        data object Retry : Action
        data object ToggleSaved : Action
        data object ToggleOverview : Action
        data object Back : Action
        data object Share : Action
        data object OpenDirections : Action
        data object Call : Action
        data object OpenHomepage : Action
        data class OpenPhotoViewer(val index: Int) : Action
        data object ClosePhotoViewer : Action
        data class OpenNearby(val contentId: Long) : Action
    }

    /**
     * ViewModel 은 Context 도 Intent 도 모른다.
     * "무엇을 열어야 하는지"만 알리고 실제 실행은 화면이 한다.
     */
    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
        data class OpenMap(val latitude: Double, val longitude: Double, val label: String) : Effect
        data class Dial(val tel: String) : Effect
        data class OpenUrl(val url: String) : Effect
        data class ShareFestival(val title: String, val text: String) : Effect
    }
}
