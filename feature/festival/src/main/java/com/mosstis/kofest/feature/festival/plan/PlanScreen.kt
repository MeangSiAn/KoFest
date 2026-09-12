package com.mosstis.kofest.feature.festival.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.model.TravelPlanRequest
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.home.HomeHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 자동 일정 짜기 (기획서 09).
 *
 * 입력 → 만드는 중 → 결과가 **한 화면 안에서** 바뀐다. 조건은 칩으로 바꾸되
 * 칩마다 다시 만들지 않는다 — 고르는 중에 화면이 계속 바뀌면 어수선하다.
 */
@Composable
fun PlanScreen(
    uiState: PlanContract.State,
    onAction: (PlanContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(PlanContract.Action.SelectLanguage(it)) },
            onSearch = {},
            showSearch = false,
        )

        when (uiState.phase) {
            PlanContract.Phase.INPUT -> PlanInput(uiState, onAction)
            PlanContract.Phase.BUILDING -> PlanBuilding()
            PlanContract.Phase.RESULT -> PlanResult(uiState, onAction)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanInput(
    uiState: PlanContract.State,
    onAction: (PlanContract.Action) -> Unit,
) {
    val s = strings()
    var showDatePicker by remember { mutableStateOf(false) }
    var showNightsMenu by remember { mutableStateOf(false) }

    if (showDatePicker) {
        // 오늘부터 6개월 뒤까지. 과거는 못 고르고, 그 이상은 후보가 바닥난다 (기획서 09).
        val today = LocalDate.now()
        val zone = ZoneOffset.UTC
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.from.atStartOfDay(zone).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(zone).toLocalDate()
                    return !date.isBefore(today) && !date.isAfter(uiState.maxDate)
                }
            },
        )
        // 확인 버튼 문구가 i18n 에 없다. 날짜를 누르면 바로 반영하고 닫는다.
        LaunchedEffect(pickerState.selectedDateMillis) {
            val millis = pickerState.selectedDateMillis ?: return@LaunchedEffect
            val picked = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
            if (picked != uiState.from) {
                onAction(PlanContract.Action.SelectDate(picked))
                showDatePicker = false
            }
        }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {},
            colors = DatePickerDefaults.colors(containerColor = KoFestColors.Paper),
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = KoFestColors.Paper,
                    selectedDayContainerColor = KoFestColors.Jaju,
                    selectedDayContentColor = KoFestColors.OnJaju,
                    todayContentColor = KoFestColors.Jaju,
                    todayDateBorderColor = KoFestColors.Jaju,
                ),
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "title") {
            Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
                Text(text = s.plan.title, style = KoFestTheme.type.screenTitle, color = KoFestColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text(text = s.plan.lead, style = KoFestTheme.type.rowPlace, color = KoFestColors.Muted)
                Spacer(Modifier.height(26.dp))
            }
        }

        item(key = "area") {
            FieldLabel(s.plan.where)
            // 시도 17개로 나누면 후보가 모자라 일정이 빈다. 권역 7개로 묶는다.
            ChipGrid(
                items = TravelPlanRequest.Area.entries,
                selected = uiState.area,
                label = { it.label() },
                onSelect = { onAction(PlanContract.Action.SelectArea(it)) },
            )
        }

        item(key = "when") {
            FieldLabel(s.plan.whenLabel)
            // 기획서 09 의 상자 둘: [10.15 / 수요일] [2박 3일 / 10.17]. 스테퍼·칩을 두지 않는다.
            Row(
                modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                DateBox(
                    title = DATE_FORMAT.format(uiState.from),
                    sub = uiState.from.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.weight(1f)) {
                    DateBox(
                        title = uiState.nights.nightsLabel(),
                        sub = DATE_FORMAT.format(uiState.until),
                        onClick = { showNightsMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = showNightsMenu,
                        onDismissRequest = { showNightsMenu = false },
                        containerColor = KoFestColors.Paper,
                    ) {
                        (0..MAX_NIGHTS).forEach { nights ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = nights.nightsLabel(),
                                        style = KoFestTheme.type.rowTitle,
                                        color = if (nights == uiState.nights) KoFestColors.Jaju else KoFestColors.Ink,
                                    )
                                },
                                onClick = {
                                    showNightsMenu = false
                                    onAction(PlanContract.Action.SelectNights(nights))
                                },
                            )
                        }
                    }
                }
            }
        }

        item(key = "how") {
            FieldLabel(s.plan.how)
            Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
                TravelPlanRequest.Mood.entries.forEach { mood ->
                    MoodRow(
                        mood = mood,
                        selected = mood == uiState.mood,
                        onClick = { onAction(PlanContract.Action.SelectMood(mood)) },
                    )
                }
            }
        }

        item(key = "go") {
            Spacer(Modifier.height(28.dp))
            Text(
                text = s.plan.build,
                style = KoFestTheme.type.rowTitle,
                color = KoFestColors.OnJaju,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = KoFestDimens.ScreenMargin)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(KoFestColors.Jaju)
                    .clickable { onAction(PlanContract.Action.Build) }
                    .padding(vertical = 16.dp),
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

/**
 * 만드는 동안. 막대는 끝까지 차오르지 않는다 — 100% 로 미리 채우면 거짓말이 된다.
 */
@Composable
private fun PlanBuilding() {
    val s = strings()
    // 2초 동안 여섯 문구를 차례로 보여준다. 막대는 90% 에서 멈춘다 — 100% 로 미리 채우면 거짓말이 된다.
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(LOADING_STEP_MILLIS)
            step = (step + 1) % s.plan.loading.size
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KoFestDimens.ScreenMargin),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(KoFestColors.Skeleton),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(3.dp)
                    .background(KoFestColors.Jaju),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(text = s.plan.loading[step], style = KoFestTheme.type.rowPlace, color = KoFestColors.Muted)
    }
}

@Composable
private fun PlanResult(
    uiState: PlanContract.State,
    onAction: (PlanContract.Action) -> Unit,
) {
    val s = strings()
    val plan = uiState.plan

    when {
        uiState.notReady || uiState.hasError || plan == null -> EmptyState(
            title = s.error.loadFailed,
            body = s.error.loadFailedHint,
            actionLabel = s.action.retry,
            onAction = { onAction(PlanContract.Action.Build) },
        )

        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "head") {
                Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
                    // 서버 응답에 title 이 없다. 권역 이름은 서버가 언어에 맞춰 준 것을 쓴다.
                    Text(
                        text = s.plan.resultTitle.fill(
                            "area" to (plan.areaName ?: uiState.area.label()),
                            "nights" to (if (uiState.nights == 0) s.plan.nights0 else s.plan.nights.fill("n" to uiState.nights, "m" to uiState.nights + 1)),
                        ),
                        style = KoFestTheme.type.screenTitle,
                        color = KoFestColors.Ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = listOf(
                            s.plan.summary.fill(
                                "range" to "${DATE_FORMAT.format(uiState.from)} – ${DATE_FORMAT.format(uiState.until)}",
                                "mood" to (plan.moodName ?: uiState.mood.label()),
                                "total" to plan.total,
                            ),
                            s.plan.withFestivals.fill("n" to plan.festivalCount),
                        ).joinToString(" · "),
                        style = KoFestTheme.type.rowWhen,
                        color = KoFestColors.Muted,
                    )
                    // 빈 일정을 그럴듯하게 꾸미지 않는다.
                    if (plan.thin) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = s.plan.thin,
                            style = KoFestTheme.type.rowPlace,
                            color = KoFestColors.OfflineInk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KoFestColors.OfflineBackground)
                                .padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            plan.days.forEachIndexed { index, day ->
                item(key = "day-${day.date}") { DayHeader(index = index + 1, day = day) }
                day.stops.forEachIndexed { stopIndex, stop ->
                    item(key = "stop-${day.date}-$stopIndex") {
                        StopRow(stop = stop, onAction = onAction)
                    }
                }
            }

            item(key = "note") {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = s.plan.distanceNote,
                    style = KoFestTheme.type.rowWhen,
                    color = KoFestColors.Muted,
                    modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin),
                )
                Spacer(Modifier.height(22.dp))
                // 조건을 바꿔 다시 짠다 (i18n plan.rebuild). 결과는 저장하지 않으므로 입력으로 돌아가면 된다.
                Text(
                    text = s.plan.rebuild,
                    style = KoFestTheme.type.rowTitle,
                    color = KoFestColors.Jaju,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = KoFestDimens.ScreenMargin)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, KoFestColors.Jaju, RoundedCornerShape(2.dp))
                        .clickable { onAction(PlanContract.Action.BackToInput) }
                        .padding(vertical = 14.dp),
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun DayHeader(index: Int, day: TravelPlan.Day) {
    val s = strings()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 26.dp, bottom = 10.dp),
    ) {
        Text(
            text = s.plan.day.fill("n" to index),
            style = KoFestTheme.type.sectionTitle,
            color = KoFestColors.Jaju,
        )
        Text(
            text = listOfNotNull(
                "${DATE_FORMAT.format(day.date)} ${day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                day.label,
            ).joinToString(" · "),
            style = KoFestTheme.type.rowWhen,
            color = KoFestColors.Muted,
        )
    }
}

@Composable
private fun StopRow(stop: TravelPlan.Stop, onAction: (PlanContract.Action) -> Unit) {
    val s = strings()
    val clickable = stop.contentId != null && stop.kind != TravelPlan.Kind.MEAL

    // 식사는 카드가 아니라 한 줄이다. 누를 수 없는 것을 카드로 만들면 눌러보게 된다.
    if (stop.kind == TravelPlan.Kind.MEAL) {
        Text(
            text = listOfNotNull(stop.time, stop.title.ifBlank { s.plan.dinner }, stop.region)
                .joinToString(" · "),
            style = KoFestTheme.type.rowWhen,
            color = KoFestColors.Muted,
            modifier = Modifier
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 10.dp),
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) {
                    Modifier.clickable {
                        onAction(PlanContract.Action.OpenStop(stop.kind, stop.contentId!!))
                    }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stop.time.orEmpty(),
                style = KoFestTheme.type.rowWhen,
                color = KoFestColors.Jaju,
                modifier = Modifier.width(46.dp),
            )
            FestivalImage(
                url = stop.thumbUrl,
                title = stop.title,
                hanjaSize = 20.sp,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (stop.kind == TravelPlan.Kind.FESTIVAL) {
                    Text(text = s.plan.festivalTag, style = KoFestTheme.type.badge, color = KoFestColors.Hwangto)
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = stop.title,
                    style = KoFestTheme.type.rowTitle,
                    color = KoFestColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    stop.region,
                    stop.stayMinutes?.let { s.plan.stay.fill("n" to it) },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(text = meta, style = KoFestTheme.type.rowWhen, color = KoFestColors.Muted)
                }
            }
        }

        stop.move?.let { move ->
            Text(
                // transit 은 소요 시간을 주지 않는다 — 없는 값을 지어내지 않는다. km 는 뒤에 붙인다.
                text = listOfNotNull(
                    when (move.mode) {
                        TravelPlan.Mode.TRANSIT -> s.plan.transit
                        TravelPlan.Mode.WALK -> s.plan.walk.fill("n" to (move.minutes ?: 0))
                    },
                    move.km?.let { "${it}km" },
                ).joinToString(" · "),
                style = KoFestTheme.type.rowWhen,
                color = KoFestColors.Muted,
                modifier = Modifier.padding(start = 58.dp, top = 8.dp),
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = KoFestTheme.type.sectionSub,
        color = KoFestColors.Muted,
        modifier = Modifier
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun <T> ChipGrid(
    items: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
        items.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    val isSelected = item == selected
                    Text(
                        text = label(item),
                        style = KoFestTheme.type.chip,
                        color = if (isSelected) KoFestColors.OnJaju else KoFestColors.Jaju,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) KoFestColors.Jaju else Color.Transparent)
                            .border(1.dp, KoFestColors.Jaju, RoundedCornerShape(20.dp))
                            .clickable { onSelect(item) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** 기획서 09 `.pl-dbox` 한 칸 — 선 테두리, 숫자는 세리프 자주색, 아래 보조 글 */
@Composable
private fun DateBox(
    title: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .border(1.dp, KoFestColors.Line, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Text(text = title, style = KoFestTheme.type.dateNumeral.copy(fontSize = 17.sp), color = KoFestColors.Jaju)
        Spacer(Modifier.height(2.dp))
        Text(text = sub, style = KoFestTheme.type.rowWhen, color = KoFestColors.Muted)
    }
}

@Composable
private fun Int.nightsLabel(): String {
    val s = strings().plan
    return if (this == 0) s.nights0 else s.nights.fill("n" to this, "m" to this + 1)
}

@Composable
private fun MoodRow(mood: TravelPlanRequest.Mood, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) KoFestColors.NoticeBackground else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
    ) {
        Text(
            text = mood.label(),
            style = KoFestTheme.type.rowTitle,
            color = if (selected) KoFestColors.Jaju else KoFestColors.Ink,
        )
        Spacer(Modifier.height(3.dp))
        // 이름만으로는 뭐가 다른지 모르기 때문에 설명을 함께 둔다.
        Text(text = mood.hint(), style = KoFestTheme.type.rowWhen, color = KoFestColors.Muted)
    }
}

@Composable
private fun TravelPlanRequest.Area.label(): String {
    val s = strings().area
    return when (this) {
        TravelPlanRequest.Area.SEOUL -> s.seoul
        TravelPlanRequest.Area.GYEONGGI -> s.gyeonggi
        TravelPlanRequest.Area.GANGWON -> s.gangwon
        TravelPlanRequest.Area.CHUNGCHEONG -> s.chungcheong
        TravelPlanRequest.Area.JEOLLA -> s.jeolla
        // 일정의 권역은 7개다 — 홈의 8개와 달리 부산·울산이 경상에 들어간다 (기획서 09)
        TravelPlanRequest.Area.GYEONGSANG -> s.gyeongsang
        TravelPlanRequest.Area.JEJU -> s.jeju
    }
}

@Composable
private fun TravelPlanRequest.Mood.label(): String {
    val s = strings().plan
    return when (this) {
        TravelPlanRequest.Mood.FESTIVAL -> s.moodFestival
        TravelPlanRequest.Mood.EASY -> s.moodEasy
        TravelPlanRequest.Mood.MANY -> s.moodMany
    }
}

@Composable
private fun TravelPlanRequest.Mood.hint(): String {
    val s = strings().plan
    return when (this) {
        TravelPlanRequest.Mood.FESTIVAL -> s.moodFestivalDesc
        TravelPlanRequest.Mood.EASY -> s.moodEasyDesc
        TravelPlanRequest.Mood.MANY -> s.moodManyDesc
    }
}

private const val LOADING_STEP_MILLIS = 380L

/** 기획서 09: 당일 · 1박 2일 · 2박 3일 · 3박 4일까지 */
private const val MAX_NIGHTS = 3
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
