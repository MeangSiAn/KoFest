package com.mosstis.kofest.feature.festival.my

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.SavedFestival

object MyContract {

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        /** 끝나지 않은 것. 시작일이 가까운 순 — 저장한 순서가 아니다 */
        val upcoming: List<SavedFestival> = emptyList(),
        /** 끝난 것. 지우지 않고 아래로 접는다 — 다녀온 기록이기도 하다 */
        val past: List<SavedFestival> = emptyList(),
        val pastExpanded: Boolean = false,
        val isLoading: Boolean = true,
        /** 이미지 캐시 크기. 아직 못 쟀으면 null */
        val cacheBytes: Long? = null,
        /** 서버의 `minAppVersion` 이 설치된 버전보다 높다. 홈을 한 번 부른 뒤에야 알 수 있다 */
        val updateAvailable: Boolean = false,
        val showLanguageDialog: Boolean = false,
        val showClearCacheDialog: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && upcoming.isEmpty() && past.isEmpty()
    }

    sealed interface Action : UiAction {
        data class OpenFestival(val language: AppLanguage, val contentId: Long) : Action
        data class Unsave(val language: AppLanguage, val festival: Festival) : Action
        data object TogglePast : Action
        data object BrowseFestivals : Action
        data object OpenLanguageDialog : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data object NotifyTap : Action
        data class OpenLink(val url: String) : Action
        data object ClearCacheTap : Action
        data object ConfirmClearCache : Action
        data object DismissDialogs : Action
        data class CacheSizeMeasured(val bytes: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
        data object NavigateToList : Effect
        data class OpenUrl(val url: String) : Effect
        /** 알림은 자리만 있다. "준비 중입니다" */
        data object ShowNotifySoon : Effect
        /** 이미지 캐시만 지운다. 저장한 축제는 건드리지 않는다 */
        data object ClearImageCache : Effect
    }
}
