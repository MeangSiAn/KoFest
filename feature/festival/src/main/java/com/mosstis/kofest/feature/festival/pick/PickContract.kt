package com.mosstis.kofest.feature.festival.pick

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.PickedPlace
import com.mosstis.kofest.domain.festival.repository.GeoPoint

/**
 * 오늘 뭐하지 — 주변에서 한 곳 (기획서 11).
 *
 * 위치를 아직 안 물었으면 안내([Phase.ASK]), 허락했으면 바로 카드([Phase.STAGE]).
 * 권한은 기기가 기억하므로 두 번째부터는 안내를 건너뛴다.
 */
object PickContract {

    enum class Phase { ASK, STAGE }

    /** 카드 아래 한 줄. 결과가 없을 때만 있다 */
    sealed interface Message {
        /** 그 반경에 아무것도 없다. [km] 는 서버가 마지막으로 넓힌 반경 */
        data class NoneInRange(val km: Int) : Message
        /** 반경 안을 다 봤다. 다음 누름은 처음부터 */
        data class AllSeen(val total: Int) : Message
        data object Failed : Message
        data object LocationFailed : Message
    }

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val phase: Phase = Phase.ASK,
        /** 권한 대화상자에서 거부했다. 안내 문구를 바꾸고 '지역으로' 를 남긴다 */
        val permissionDenied: Boolean = false,
        val isLocating: Boolean = false,
        /** 뽑는 데만 쓴다. 저장하지 않는다 — ViewModel 이 사라지면 같이 사라진다 */
        val location: GeoPoint? = null,
        val isDrawing: Boolean = false,
        val picked: PickedPlace? = null,
        /** [picked] 가 어느 언어의 데이터인지 — 상세 경로는 이 언어로 만든다 */
        val pickedLanguage: AppLanguage = AppLanguage.KO,
        /** 이번에 쓴 반경 · 후보 수 · 몇 번째 */
        val km: Int = 0,
        val total: Int = 0,
        val seen: Int = 0,
        /** 이미 나온 곳. 같은 곳이 두 번 나오면 "이거 랜덤 맞나" 싶어진다 */
        val excluded: List<Long> = emptyList(),
        /** 다 봤다. 다음 누름은 [excluded] 를 비우고 처음부터 */
        val exhausted: Boolean = false,
        val message: Message? = null,
    ) : UiState {
        val hasResult: Boolean get() = picked != null
        val canDraw: Boolean get() = !isDrawing && !isLocating
    }

    sealed interface Action : UiAction {
        /** 화면에 들어왔다. 권한이 이미 있으면 안내를 건너뛴다 */
        data object ScreenStarted : Action
        /** '위치 켜고 시작하기'. 권한 대화상자는 화면이 띄운다 */
        data object RequestLocation : Action
        data class PermissionResult(val granted: Boolean) : Action
        /** 뽑기 · 다시 뽑기 · 처음부터 다시 — 상태가 정한다 */
        data object Draw : Action
        data object Go : Action
        data object BrowseByRegion : Action
        data class SelectLanguage(val language: AppLanguage) : Action
    }

    sealed interface Effect : UiEffect {
        data object RequestPermission : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
        data object NavigateToList : Effect
    }
}
