package com.mosstis.kofest.feature.festival.list

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.LanguageCounts
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import java.time.LocalDate
import java.time.YearMonth

object ListContract {

    /**
     * 목록 화면 상단의 전환. 달력은 별도 탭이 아니라 이 화면의 다른 보기 방식이다 —
     * 둘 다 "언제 어디서 열리나"를 묻는 화면이라 [State.filter] 를 공유한다.
     */
    enum class Mode { LIST, CALENDAR }

    /**
     * 무엇을 둘러보는가. 축제와 관광지는 **다른 테이블**이고 정렬 축도 다르다 —
     * 축제는 날짜(월 헤더), 관광지는 지역(시도 헤더)으로 묶는다 (기획서 03).
     */
    enum class Tab { FESTIVAL, PLACE }

    /** 관광 탭의 한 덩어리. 축제의 월 헤더 자리에 시도 헤더가 온다 */
    data class PlaceSection(
        val region: String,
        val places: List<Place>,
    )

    /** 월 헤더 하나와 그 아래 행들. 월 헤더는 스크롤 시 상단에 고정된다. */
    data class MonthSection(
        val month: LocalDate,
        val festivals: List<Festival>,
    )

    /**
     * 달력 보기의 상태. 월 단위 캐시라 이전·다음 달을 미리 받아두면 스와이프가 끊기지 않는다.
     * [koMonths] 는 영어 화면에서 "한국어로 보면 이날 N건" 안내를 위해 따로 받는다.
     */
    data class Calendar(
        val month: YearMonth = YearMonth.now(),
        /** 누른 날. 월을 넘기면 해제되고 목록은 그 달 전체로 */
        val selected: LocalDate? = null,
        val months: Map<YearMonth, List<Festival>> = emptyMap(),
        val koMonths: Map<YearMonth, List<Festival>> = emptyMap(),
        val loading: Set<YearMonth> = emptySet(),
        val failed: Set<YearMonth> = emptySet(),
    ) {
        val current: List<Festival>? get() = months[month]
        val failedCurrent: Boolean get() = month in failed && current == null
    }

    data class State(
        val language: AppLanguage = AppLanguage.KO,
        val mode: Mode = Mode.LIST,
        val filter: FestivalFilter = FestivalFilter(),
        val sections: List<MonthSection> = emptyList(),
        /** [sections] 가 어느 언어의 데이터인지 */
        val sectionsLanguage: AppLanguage = AppLanguage.KO,
        val counts: LanguageCounts = LanguageCounts.EMPTY,
        val nextCursor: String? = null,
        /**
         * 이 달의 섹션으로 화면을 옮겨야 한다. 옮기고 나면 지운다.
         *
         * '이번 달'을 걸어도 그 달에 **걸치는** 연중 상설 행사가 목록 앞을 채운다
         * (9월 기준 46건). 정렬이 `start_date` 라 그 달 섹션은 한참 아래에 있어,
         * 필터만 걸고 두면 눌러도 아무 일 없는 것처럼 보인다.
         */
        val scrollToMonth: LocalDate? = null,
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isStale: Boolean = false,
        val hasFatalError: Boolean = false,
        val calendar: Calendar = Calendar(),
        val tab: Tab = Tab.FESTIVAL,
        val placeFilter: PlaceFilter = PlaceFilter(),
        val placeSections: List<PlaceSection> = emptyList(),
        val placeCursor: String? = null,
        val placeTotal: Int? = null,
        val isLoadingPlaces: Boolean = false,
        /** `GET /places` 가 서버에 아직 없다. 오류가 아니라 준비 중이다 */
        val placesNotReady: Boolean = false,
        val placesFailed: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && sections.isEmpty() && !hasFatalError
        val showSkeleton: Boolean get() = isLoading && sections.isEmpty()
        val hasMore: Boolean get() = nextCursor != null

        val placesEmpty: Boolean
            get() = !isLoadingPlaces && placeSections.isEmpty() && !placesNotReady && !placesFailed
        val hasMorePlaces: Boolean get() = placeCursor != null
    }

    sealed interface Action : UiAction {
        data object ScreenStarted : Action
        data object Retry : Action
        data object LoadMore : Action
        data object ClearFilters : Action

        /** 화면을 옮겼다. 표시를 지운다 */
        data object ScrollHandled : Action
        data object ToggleOngoing : Action
        data object ToggleHasImage : Action
        /** 광역권이면 시도코드가 여럿이다. 비어 있으면 전국 */
        data class SelectRegion(val regionCodes: List<String>) : Action
        data object ToggleThisMonth : Action
        data class SelectLanguage(val language: AppLanguage) : Action
        data class OpenFestival(val contentId: Long) : Action

        // 달력 보기
        data class SelectMode(val mode: Mode) : Action
        data object PrevMonth : Action
        data object NextMonth : Action
        data class SelectDay(val date: LocalDate) : Action
        data object RetryMonth : Action

        // 관광 탭
        data class SelectTab(val tab: Tab) : Action
        data class SelectPlaceType(val type: Int?) : Action
        data object LoadMorePlaces : Action
        data object RetryPlaces : Action
        data class OpenPlace(val contentId: Long) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val language: AppLanguage, val contentId: Long) : Effect
        data class NavigateToPlace(val language: AppLanguage, val contentId: Long) : Effect
    }
}
