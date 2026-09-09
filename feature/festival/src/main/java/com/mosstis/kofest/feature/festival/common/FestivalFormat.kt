package com.mosstis.kofest.feature.festival.common

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.ui.strings.KoFestStrings
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 도메인 모델 → 화면 문자열.
 *
 * 표시 규칙만 담는다. 진행 상태(ongoing/upcoming/ended)는 **서버가 정한 값**을 그대로 쓰고,
 * 여기서 오늘 날짜와 다시 비교하지 않는다.
 */
object FestivalFormat {

    private val koDate = DateTimeFormatter.ofPattern("MM.dd", Locale.KOREA)
    private val enDate = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

    fun date(date: LocalDate, language: AppLanguage): String = when (language) {
        AppLanguage.KO -> koDate.format(date)
        AppLanguage.EN -> enDate.format(date)
    }

    /** 하루짜리 행사는 범위로 쓰지 않는다. */
    fun period(festival: Festival, language: AppLanguage): String =
        if (festival.isSingleDay) {
            date(festival.startDate, language)
        } else {
            "${date(festival.startDate, language)} – ${date(festival.endDate, language)}"
        }

    fun dateRange(range: ClosedRange<LocalDate>, language: AppLanguage): String =
        "${date(range.start, language)} – ${date(range.endInclusive, language)}"

    /** 목록 행 왼쪽 열의 요일. 세리프 라틴 서체로 쓰므로 항상 영문 약어다. */
    fun dayOfWeekShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    fun dayOfMonth(date: LocalDate): String = "%02d".format(date.dayOfMonth)

    /** 월 헤더의 큰 글씨. Cormorant 로 쓰므로 영문 월 이름이다. */
    fun monthName(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    /**
     * @param currentYear 오늘의 연도. 다른 해의 월이면 연도를 함께 보여준다 —
     *  실제 데이터에는 몇 해 전에 시작해 지금도 진행중인 상설 행사가 섞여 있어,
     *  연도가 없으면 "November 다음 January" 처럼 읽힌다.
     */
    fun monthSub(
        date: LocalDate,
        count: Int,
        strings: KoFestStrings,
        language: AppLanguage,
        currentYear: Int,
    ): String {
        val sameYear = date.year == currentYear
        return when (language) {
            AppLanguage.KO -> strings.list.monthCount.fill(
                "month" to if (sameYear) "${date.monthValue}월" else "${date.year}년 ${date.monthValue}월",
                "count" to count,
            )

            AppLanguage.EN -> strings.list.monthCount.fill(
                "month" to if (sameYear) monthName(date) else "${monthName(date)} ${date.year}",
                "count" to count,
            )
        }
    }

    /**
     * 배지 문구. 종료일이 오늘이면 '오늘 마지막'.
     *
     * @param today 상대 일수를 만들기 위한 오늘 날짜. 상태 판정 자체는 서버 값을 쓴다.
     */
    fun stateLabel(
        festival: Festival,
        today: LocalDate,
        strings: KoFestStrings,
    ): String? = when (festival.state) {
        FestivalState.ONGOING ->
            if (festival.endDate == today) strings.state.lastDay else strings.state.ongoing

        FestivalState.UPCOMING -> {
            val days = ChronoUnit.DAYS.between(today, festival.startDate)
            when {
                days <= 0L -> strings.state.today
                days == 1L -> strings.state.tomorrow
                days <= RELATIVE_DAYS_LIMIT -> strings.state.upcoming.fill("days" to days)
                // 한 달 넘게 남은 것에 "37일 뒤"는 정보가 아니라 소음이다.
                else -> null
            }
        }

        FestivalState.ENDED -> strings.state.ended
    }

    /**
     * 상세 상단의 상태 한 줄. 목록 배지와 달리 "8일 뒤 **시작**" 처럼 문장으로 쓴다.
     * 한 달 넘게 남았으면 아무것도 쓰지 않는다.
     */
    fun detailStateLabel(
        festival: Festival,
        today: LocalDate,
        strings: KoFestStrings,
    ): String? = when (festival.state) {
        FestivalState.ONGOING ->
            if (festival.endDate == today) strings.state.endsToday else strings.state.ongoing

        FestivalState.UPCOMING -> {
            val days = ChronoUnit.DAYS.between(today, festival.startDate)
            when {
                days <= 0L -> strings.state.today
                days == 1L -> strings.state.tomorrow
                days <= RELATIVE_DAYS_LIMIT -> strings.state.startsIn.fill("days" to days)
                else -> null
            }
        }

        FestivalState.ENDED -> strings.state.ended
    }

    /** "4일간" / "하루". 시작일과 종료일을 모두 세는 것이 축제 관례다 */
    fun duration(festival: Festival, strings: KoFestStrings): String {
        val days = ChronoUnit.DAYS.between(festival.startDate, festival.endDate) + 1
        return if (days <= 1) strings.detail.oneDay else strings.detail.days.fill("days" to days)
    }

    private const val RELATIVE_DAYS_LIMIT = 21L
}
