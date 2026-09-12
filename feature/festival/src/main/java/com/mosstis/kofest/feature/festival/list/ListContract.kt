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
import com.mosstis.kofest.domain.festival.model.RegionBucket
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

    /** 필터 칩을 눌렀을 때 뜨는 선택 시트 (기획서 03 · 필터) */
    enum class Sheet { REGION, PERIOD, TYPE }

    /** 시기. 기간 지정은 [Action.SelectDateRange] 로 따로 온다 */
    enum class Period { ALL, THIS_MONTH, NEXT_MONTH }

    /** 관광 탭 유형 필터. TourAPI `contentTypeId` 를 그대로 쓴다 — 숙박(32)은 넣지 않는다 */
    val PLACE_TYPES = listOf(12, 14, 28, 38, 39)

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
        val isLoadingMorePlaces: Boolean = false,
        /** `GET /places` 가 서버에 아직 없다. 오류가 아니라 준비 중이다 */
        val placesNotReady: Boolean = false,
        val placesFailed: Boolean = false,
        // 필터 선택
        val sheet: Sheet? = null,
        /** 시도 목록. 지역 시트를 처음 열 때 `/home` 에서 받는다 — 이름과 건수를 서버가 준다 */
        val regions: List<RegionBucket> = emptyList(),
        val isLoadingRegions: Boolean = false,
        val regionsFailed: Boolean = false,
        val showDateRange: Boolean = false,
    ) : UiState {
        /** 칩에 쓸 시기. 지정한 기간이 이번 달·다음 달과 정확히 같으면 그 이름으로 */
        val period: Period
            get() {
                val from = filter.from ?: return Period.ALL
                val to = filter.to
                val thisMonth = YearMonth.from(LocalDate.now())
                return when {
                    from == thisMonth.atDay(1) && to == thisMonth.atEndOfMonth() -> Period.THIS_MONTH
                    from == thisMonth.plusMonths(1).atDay(1) && to == thisMonth.plusMonths(1).atEndOfMonth() -> Period.NEXT_MONTH
                    else -> Period.ALL
                }
            }

        /** 이번 달·다음 달이 아닌 직접 고른 기간 */
        val customRange: ClosedRange<LocalDate>?
            get() {
                val from = filter.from ?: return null
                val to = filter.to ?: return null
                return if (period == Period.ALL) from..to else null
            }

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
        data class OpenSheet(val sheet: Sheet) : Action
        data object CloseSheet : Action
        data object RetryRegions : Action
        data class SelectPeriod(val period: Period) : Action
        data object OpenDateRange : Action
        data object CloseDateRange : Action
        data class SelectDateRange(val from: LocalDate, val to: LocalDate) : Action
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
