package com.mosstis.kofest.feature.festival.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 필터 칩을 눌렀을 때의 선택 시트 (기획서 03 · 필터).
 *
 * 지역은 시도 단위다 — 기획서는 "광역권 → 시군구 2단계" 라 적었지만 서버 필터가 시도까지라
 * (`legal_dong` 미적재) 시도 하나로 고른다. 이름과 건수는 `/home` 의 `regions` 를 그대로 쓴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListFilterSheets(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()

    uiState.sheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { onAction(ListContract.Action.CloseSheet) },
            containerColor = KoFestColors.Paper,
            dragHandle = null,
        ) {
            when (sheet) {
                ListContract.Sheet.REGION -> RegionSheet(uiState, onAction)
                ListContract.Sheet.PERIOD -> PeriodSheet(uiState, onAction)
                ListContract.Sheet.TYPE -> TypeSheet(uiState, onAction)
            }
        }
    }

    if (uiState.showDateRange) {
        DateRangeDialog(
            initial = uiState.customRange,
            onSelect = { from, to -> onAction(ListContract.Action.SelectDateRange(from, to)) },
            onDismiss = { onAction(ListContract.Action.CloseDateRange) },
        )
    }
}

@Composable
private fun RegionSheet(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()
    val selected = uiState.filter.regionCodes
    val placeTab = uiState.tab == ListContract.Tab.PLACE

    Column(modifier = Modifier.navigationBarsPadding()) {
        SheetTitle(s.filter.region)
        when {
            uiState.isLoadingRegions -> Column(Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
                repeat(6) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 20.dp)
                    Spacer(Modifier.height(24.dp))
                }
            }
            uiState.regionsFailed -> SheetRow(
                label = s.action.retry,
                trailing = s.error.loadFailed,
                selected = false,
                onClick = { onAction(ListContract.Action.RetryRegions) },
            )
            else -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item(key = "all") {
                    SheetRow(
                        label = s.filter.regionAll,
                        trailing = null,
                        selected = selected.isEmpty(),
                        onClick = { onAction(ListContract.Action.SelectRegion(emptyList())) },
                    )
                }
                items(uiState.regions, key = { it.code }) { region ->
                    val count = if (placeTab) region.places else region.count
                    SheetRow(
                        label = region.name,
                        trailing = if (placeTab) s.place.count.fill("count" to count) else s.list.count.fill("count" to count),
                        selected = selected.size == 1 && selected.first() == region.code,
                        onClick = { onAction(ListContract.Action.SelectRegion(listOf(region.code))) },
                    )
                }
                item(key = "bottom") { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun PeriodSheet(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()
    val custom = uiState.customRange

    Column(modifier = Modifier.navigationBarsPadding()) {
        SheetTitle(s.filter.period)
        SheetRow(
            label = s.filter.periodAll,
            trailing = null,
            selected = uiState.period == ListContract.Period.ALL && custom == null,
            onClick = { onAction(ListContract.Action.SelectPeriod(ListContract.Period.ALL)) },
        )
        SheetRow(
            label = s.filter.thisMonth,
            trailing = null,
            selected = uiState.period == ListContract.Period.THIS_MONTH,
            onClick = { onAction(ListContract.Action.SelectPeriod(ListContract.Period.THIS_MONTH)) },
        )
        SheetRow(
            label = s.filter.nextMonth,
            trailing = null,
            selected = uiState.period == ListContract.Period.NEXT_MONTH,
            onClick = { onAction(ListContract.Action.SelectPeriod(ListContract.Period.NEXT_MONTH)) },
        )
        SheetRow(
            label = s.filter.custom,
            trailing = custom?.let { FestivalFormat.dateRange(it, uiState.language) },
            selected = custom != null,
            onClick = { onAction(ListContract.Action.OpenDateRange) },
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TypeSheet(
    uiState: ListContract.State,
    onAction: (ListContract.Action) -> Unit,
) {
    val s = strings()
    val selected = uiState.placeFilter.type

    Column(modifier = Modifier.navigationBarsPadding()) {
        SheetTitle(s.place.filterType)
        SheetRow(
            label = s.place.typeAll,
            trailing = null,
            selected = selected == null,
            onClick = { onAction(ListContract.Action.SelectPlaceType(null)) },
        )
        ListContract.PLACE_TYPES.forEach { type ->
            SheetRow(
                label = placeTypeName(type),
                trailing = null,
                selected = selected == type,
                onClick = { onAction(ListContract.Action.SelectPlaceType(type)) },
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

/** 유형 번호 → 이름. 서버가 `typeName` 을 주지만 필터 시트는 목록이 오기 전에 떠야 해서 i18n 을 쓴다 */
@Composable
fun placeTypeName(type: Int): String = with(strings().place) {
    when (type) {
        12 -> type12
        14 -> type14
        28 -> type28
        38 -> type38
        39 -> type39
        else -> type.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    initial: ClosedRange<LocalDate>?,
    onSelect: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = strings()
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.toEpochMillis(),
        initialSelectedEndDateMillis = initial?.endInclusive?.toEpochMillis(),
    )
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // 확인 문구가 i18n 에 없어 '기간 지정' 을 그대로 버튼으로 쓴다. 둘 다 골라야 눌린다.
            val enabled = start != null && end != null
            Text(
                text = s.filter.custom,
                style = KoFestTheme.type.button,
                color = if (enabled) KoFestColors.Jaju else KoFestColors.Muted,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = enabled) {
                        onSelect(start!!.toLocalDate(), end!!.toLocalDate())
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        },
        colors = DatePickerDefaults.colors(containerColor = KoFestColors.Paper),
    ) {
        DateRangePicker(
            state = state,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = KoFestColors.Paper,
                selectedDayContainerColor = KoFestColors.Jaju,
                selectedDayContentColor = KoFestColors.OnJaju,
                dayInSelectionRangeContainerColor = KoFestColors.CalendarToday,
                todayContentColor = KoFestColors.Jaju,
                todayDateBorderColor = KoFestColors.Jaju,
            ),
        )
    }
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun SheetTitle(title: String) {
    Text(
        text = title,
        style = KoFestTheme.type.sectionTitle,
        color = KoFestColors.Ink,
        modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin, vertical = 18.dp),
    )
}

@Composable
private fun SheetRow(
    label: String,
    trailing: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) KoFestColors.NoticeBackground else KoFestColors.Paper)
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = KoFestTheme.type.rowTitle.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (selected) KoFestColors.Jaju else KoFestColors.Ink,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = KoFestTheme.type.rowPlace,
                color = KoFestColors.Muted,
            )
        }
    }
}
