package com.mosstis.kofest.feature.festival.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalState
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import com.mosstis.kofest.feature.festival.home.HomeHeader
import com.mosstis.kofest.feature.festival.list.FestivalRow
import com.mosstis.kofest.feature.festival.list.FestivalRowSkeleton
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/**
 * 달력 — "그날 뭐가 열리나".
 *
 * 날짜를 눌러도 새 화면으로 넘어가지 않는다. 달력은 그대로 두고 아래 목록만 바뀐다.
 * 여행 계획은 여러 날짜를 오가며 세우기 때문이다.
 */
@Composable
fun CalendarScreen(
    uiState: CalendarContract.State,
    onAction: (CalendarContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()
    val festivals = uiState.current
    val byDay = remember(festivals, uiState.month) { festivals?.let { spreadByDay(it, uiState.month) } }
    val selected = uiState.selected

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(CalendarContract.Action.SelectLanguage(it)) },
            onSearch = {},
            showSearch = false,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "top") {
                MonthTop(
                    month = uiState.month,
                    count = festivals?.size,
                    onPrev = { onAction(CalendarContract.Action.PrevMonth) },
                    onNext = { onAction(CalendarContract.Action.NextMonth) },
                )
            }

            item(key = "grid") {
                MonthGrid(
                    month = uiState.month,
                    today = today,
                    selected = selected,
                    byDay = byDay,
                    onSelect = { onAction(CalendarContract.Action.SelectDay(it)) },
                    onSwipe = { forward ->
                        onAction(if (forward) CalendarContract.Action.NextMonth else CalendarContract.Action.PrevMonth)
                    },
                )
            }

            when {
                uiState.failedCurrent -> item(key = "error") {
                    EmptyState(
                        title = s.error.loadFailed,
                        body = s.error.loadFailedHint,
                        actionLabel = s.action.retry,
                        onAction = { onAction(CalendarContract.Action.Retry) },
                    )
                }

                festivals == null || byDay == null -> {
                    item(key = "loading-head") { Spacer(Modifier.height(22.dp)) }
                    items(count = 3, key = { "skeleton-$it" }) { FestivalRowSkeleton() }
                }

                selected != null -> {
                    val onDay = byDay[selected].orEmpty()
                    item(key = "sel-head") {
                        SelectionHeader(
                            title = selectedLabel(selected, uiState.language, s.calendar.selected),
                            sub = s.calendar.dayCount.fill("count" to onDay.size),
                        )
                    }
                    if (onDay.isEmpty()) {
                        item(key = "sel-empty") {
                            EmptyDay(
                                selected = selected,
                                byDay = byDay,
                                koCount = uiState.koMonths[uiState.month]
                                    ?.let { spreadByDay(it, uiState.month)[selected]?.size }
                                    ?: 0,
                                language = uiState.language,
                            )
                        }
                    } else {
                        items(onDay, key = { it.contentId }) { festival ->
                            FestivalRow(
                                festival = festival,
                                today = today,
                                language = uiState.language,
                                onClick = { onAction(CalendarContract.Action.OpenFestival(festival.contentId)) },
                                whenText = dayRelation(festival, selected, uiState.language),
                            )
                        }
                    }
                }

                else -> {
                    val monthList = festivals.sortedWith(compareBy({ it.startDate }, { it.contentId }))
                    item(key = "month-head") {
                        SelectionHeader(
                            title = monthLabel(uiState.month, uiState.language),
                            sub = s.calendar.monthCount.fill("count" to monthList.size),
                        )
                    }
                    items(monthList, key = { it.contentId }) { festival ->
                        FestivalRow(
                            festival = festival,
                            today = today,
                            language = uiState.language,
                            onClick = { onAction(CalendarContract.Action.OpenFestival(festival.contentId)) },
                        )
                    }
                }
            }

            item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** 월 이름 + 그 달 축제 수 + 좌우 화살표. */
@Composable
private fun MonthTop(month: YearMonth, count: Int?, onPrev: () -> Unit, onNext: () -> Unit) {
    val s = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = KoFestDimens.ScreenMargin, end = KoFestDimens.ScreenMargin - 7.dp)
            .padding(top = 6.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = FestivalFormat.monthName(month.atDay(1)) + if (month.year != LocalDate.now().year) " ${month.year}" else "",
                style = KoFestTheme.type.calMonth,
                color = KoFestColors.Jaju,
            )
            if (count != null) {
                Spacer(Modifier.size(8.dp))
                Text(
                    text = s.calendar.monthCount.fill("count" to count),
                    style = KoFestTheme.type.calMonthSub,
                    color = KoFestColors.Muted,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NavButton(KoFestIcons.ChevronLeft, s.calendar.prevMonth, onPrev)
            NavButton(KoFestIcons.ChevronRight, s.calendar.nextMonth, onNext)
        }
    }
}

@Composable
private fun NavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = KoFestColors.Ink, modifier = Modifier.size(16.dp))
    }
}

/**
 * 월요일 시작 7열 격자. 앞뒤 달의 날짜는 옅게 그리되 누를 수 있다.
 * 점은 밀도만 말한다 — 1~3개는 건수 그대로, 4건 이상은 3개로 고정. 황토색은 오늘 기준 진행중.
 */
@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate?,
    byDay: Map<LocalDate, List<Festival>>?,
    onSelect: (LocalDate) -> Unit,
    onSwipe: (forward: Boolean) -> Unit,
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - DayOfWeek.MONDAY.value   // 월=0 … 일=6
    val cells = ((leading + month.lengthOfMonth() + 6) / 7) * 7
    val start = first.minusDays(leading.toLong())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(month) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (abs(total) > SWIPE_THRESHOLD_PX) onSwipe(total < 0) },
                    onHorizontalDrag = { _, dx -> total += dx },
                )
            },
    ) {
        Row(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin).padding(bottom = 6.dp)) {
            WEEKDAY_LETTERS.forEach { letter ->
                Text(
                    text = letter,
                    style = KoFestTheme.type.calWeekday,
                    color = KoFestColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        var index = 0
        while (index < cells) {
            Row(
                modifier = Modifier
                    .padding(horizontal = KoFestDimens.ScreenMargin)
                    .padding(bottom = 2.dp),
            ) {
                repeat(7) {
                    val date = start.plusDays(index.toLong())
                    DayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        isSelected = date == selected,
                        festivals = byDay?.get(date).orEmpty(),
                        onClick = { onSelect(date) },
                        modifier = Modifier.weight(1f),
                    )
                    index++
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    festivals: List<Festival>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isSelected -> KoFestColors.Jaju
        isToday -> KoFestColors.CalendarToday
        else -> Color.Transparent
    }
    val numberColor = when {
        isSelected -> Color.White
        isToday -> KoFestColors.Jaju
        !inMonth -> KoFestColors.CalendarOutDay
        else -> KoFestColors.Ink
    }

    Column(
        modifier = modifier
            .aspectRatio(1f / 1.06f)
            .clip(RoundedCornerShape(3.dp))
            .background(background)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = date.dayOfMonth.toString(), style = KoFestTheme.type.calDay, color = numberColor)
        Spacer(Modifier.height(5.dp))
        // 점 자리는 항상 차지한다 — 점이 있고 없고에 따라 숫자 위치가 흔들리면 안 된다.
        Row(
            modifier = Modifier.height(5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            festivals
                .sortedByDescending { it.state == FestivalState.ONGOING }
                .take(MAX_DOTS)
                .forEach { festival ->
                    val dot = when {
                        isSelected -> Color.White.copy(alpha = 0.85f)
                        festival.state == FestivalState.ONGOING -> KoFestColors.Hwangto
                        else -> KoFestColors.Jaju
                    }
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dot),
                    )
                }
        }
    }
}

@Composable
private fun SelectionHeader(title: String, sub: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 22.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = title, style = KoFestTheme.type.calSelected, color = KoFestColors.Ink)
        Text(text = sub, style = KoFestTheme.type.calSelectedSub, color = KoFestColors.Muted)
    }
}

/**
 * 축제가 없는 날. 회색으로 흐리거나 누를 수 없게 만들지 않는다 — 그냥 점이 없을 뿐이다.
 * 눌렀을 때 왜 비었는지와 가까운 날짜를 알려준다. 영어에서는 한국어 안내를 덧붙인다.
 */
@Composable
private fun EmptyDay(
    selected: LocalDate,
    byDay: Map<LocalDate, List<Festival>>,
    koCount: Int,
    language: AppLanguage,
) {
    val s = strings()
    val nearest = byDay.keys
        .filter { byDay[it]!!.isNotEmpty() }
        .minByOrNull { abs(java.time.temporal.ChronoUnit.DAYS.between(selected, it)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 38.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = s.calendar.noneToday, style = KoFestTheme.type.emptyTitle, color = KoFestColors.Ink, textAlign = TextAlign.Center)
        val hints = buildList {
            if (nearest != null) add(s.calendar.noneHint.fill("date" to FestivalFormat.date(nearest, language)))
            if (language == AppLanguage.EN && koCount > 0) add(s.calendar.noneInLang.fill("count" to koCount))
        }
        if (hints.isNotEmpty()) {
            Spacer(Modifier.height(9.dp))
            Text(
                text = hints.joinToString("\n"),
                style = KoFestTheme.type.calEmpty,
                color = KoFestColors.Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 기간이 있는 축제는 걸치는 날 전부에 넣는다. 시작일에만 찍으면 16일에 가는 사람이 못 찾는다. */
private fun spreadByDay(festivals: List<Festival>, month: YearMonth): Map<LocalDate, List<Festival>> {
    val from = month.atDay(1).minusDays(7)   // 앞뒤 달의 보이는 칸까지
    val to = month.atEndOfMonth().plusDays(7)
    val map = mutableMapOf<LocalDate, MutableList<Festival>>()
    festivals.forEach { festival ->
        var day = maxOf(festival.startDate, from)
        val end = minOf(festival.endDate, to)
        while (!day.isAfter(end)) {
            map.getOrPut(day) { mutableListOf() }.add(festival)
            day = day.plusDays(1)
        }
    }
    return map
}

/** "10.15 – 10.18 · 첫날 / 진행중 / 마지막 날" — 선택한 날이 축제의 어느 지점인지. */
@Composable
private fun dayRelation(festival: Festival, selected: LocalDate, language: AppLanguage): String {
    val s = strings()
    val relation = when (selected) {
        festival.startDate -> s.calendar.firstDay
        festival.endDate -> s.calendar.lastDay
        else -> s.calendar.ongoing
    }
    return "${FestivalFormat.period(festival, language)} · $relation"
}

private fun selectedLabel(date: LocalDate, language: AppLanguage, pattern: String): String = when (language) {
    AppLanguage.KO -> pattern.fill(
        "month" to date.monthValue,
        "day" to date.dayOfMonth,
        "weekday" to date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
    )
    AppLanguage.EN -> pattern.fill(
        "weekday" to date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
        "month" to date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
        "day" to date.dayOfMonth,
    )
}

private fun monthLabel(month: YearMonth, language: AppLanguage): String = when (language) {
    AppLanguage.KO -> "${month.monthValue}월"
    AppLanguage.EN -> FestivalFormat.monthName(month.atDay(1))
}

private val WEEKDAY_LETTERS = listOf("M", "T", "W", "T", "F", "S", "S")
private const val MAX_DOTS = 3
private const val SWIPE_THRESHOLD_PX = 120f
