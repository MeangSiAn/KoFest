package com.mosstis.kofest.feature.festival.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.component.OnNowLabel
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalState
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate

@Composable
fun ListHeader(
    title: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.Paper)
            .padding(
                start = KoFestDimens.ScreenMargin,
                end = KoFestDimens.ScreenMargin,
                top = 16.dp,
                bottom = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = KoFestTheme.type.screenTitle,
            color = KoFestColors.Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = KoFestIcons.Search,
            contentDescription = strings().action.search,
            tint = KoFestColors.Ink,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onSearch),
        )
    }
}

/** 필터 줄. 칩이 아니라 밑줄 텍스트다 — 채워진 알약이 줄지어 있으면 화면이 시끄럽다. */
@Composable
fun FilterBar(
    regionLabel: String,
    regionSelected: Boolean,
    periodLabel: String,
    periodSelected: Boolean,
    ongoingSelected: Boolean,
    hasImageSelected: Boolean,
    onRegion: () -> Unit,
    onPeriod: () -> Unit,
    onOngoing: () -> Unit,
    onHasImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()

    Column(modifier = modifier.background(KoFestColors.Paper)) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = KoFestDimens.ScreenMargin)
                .padding(top = 4.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilterChip(regionLabel, regionSelected, hasMenu = true, onClick = onRegion)
            FilterChip(periodLabel, periodSelected, hasMenu = true, onClick = onPeriod)
            FilterChip(s.filter.ongoing, ongoingSelected, hasMenu = false, onClick = onOngoing)
            FilterChip(s.filter.hasImage, hasImageSelected, hasMenu = false, onClick = onHasImage)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KoFestColors.Line),
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    hasMenu: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) KoFestColors.Jaju else KoFestColors.Muted

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = KoFestTheme.type.chip.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = color,
            )
            if (hasMenu) {
                Spacer(Modifier.width(5.dp))
                Icon(
                    imageVector = KoFestIcons.ChevronDown,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.5f),
                    modifier = Modifier.size(8.dp),
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (selected) KoFestColors.Jaju else androidx.compose.ui.graphics.Color.Transparent),
        )
    }
}

/** 월 헤더. 지금 몇 월을 보고 있는지 항상 보여야 한다. */
@Composable
fun MonthHeader(
    month: LocalDate,
    count: Int,
    language: AppLanguage,
    currentYear: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.Paper)
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = FestivalFormat.monthName(month),
            style = KoFestTheme.type.monthNumeral,
            color = KoFestColors.Jaju,
        )
        Text(
            text = FestivalFormat.monthSub(month, count, strings(), language, currentYear),
            style = KoFestTheme.type.monthSub,
            color = KoFestColors.Muted,
        )
    }
}

/**
 * 목록 행.
 *
 * 날짜를 왼쪽 세로 열에 두면 스크롤하며 날짜만 훑어 내려갈 수 있다.
 * 썸네일 위에 배지를 얹지 않는다 — 행마다 반복되면 시끄럽다.
 */
@Composable
fun FestivalRow(
    festival: Festival,
    today: LocalDate,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** 기간 줄을 통째로 바꾼다. 달력은 '10.15 – 10.18 · 첫날' 처럼 선택한 날 기준으로 쓴다 */
    whenText: String? = null,
    /** 지난 축제처럼 뒤로 물러나야 할 행 */
    dimmed: Boolean = false,
) {
    val stateLabel = FestivalFormat.stateLabel(festival, today, strings())

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .alpha(if (dimmed) 0.55f else 1f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(KoFestDimens.DateColumnWidth)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = FestivalFormat.dayOfMonth(festival.startDate),
                    style = KoFestTheme.type.dateNumeral,
                    color = KoFestColors.Jaju,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = FestivalFormat.dayOfWeekShort(festival.startDate),
                    style = KoFestTheme.type.dateDayOfWeek,
                    color = KoFestColors.Muted,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = festival.title,
                    style = KoFestTheme.type.rowTitle,
                    color = KoFestColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = festival.region,
                    style = KoFestTheme.type.rowPlace,
                    color = KoFestColors.Muted,
                )
                Spacer(Modifier.height(7.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    if (whenText != null) {
                        Text(text = whenText, style = KoFestTheme.type.rowWhen, color = KoFestColors.Muted)
                    } else if (festival.state == FestivalState.ONGOING && stateLabel != null) {
                        OnNowLabel(label = stateLabel)
                    } else if (stateLabel != null) {
                        Text(
                            text = "$stateLabel ·",
                            style = KoFestTheme.type.rowWhen,
                            color = KoFestColors.Muted,
                        )
                    }
                    if (whenText == null) {
                        Text(
                            text = FestivalFormat.period(festival, language),
                            style = KoFestTheme.type.rowWhen,
                            color = KoFestColors.Muted,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(KoFestDimens.ThumbWidth)
                    .height(KoFestDimens.ThumbHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KoFestColors.JajuDeep),
            ) {
                FestivalImage(
                    url = festival.thumbUrl,
                    title = festival.title,
                    modifier = Modifier.fillMaxWidth().height(KoFestDimens.ThumbHeight),
                    hanjaSize = 26.sp,
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KoFestColors.Line),
        )
    }
}

/** 실제 행 모양 그대로의 회색 뼈대. */
@Composable
fun FestivalRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.width(KoFestDimens.DateColumnWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SkeletonBlock(Modifier.width(24.dp), height = 20.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBlock(Modifier.fillMaxWidth(0.78f), height = 14.dp)
            Spacer(Modifier.height(9.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.46f))
            Spacer(Modifier.height(9.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.6f))
        }
        SkeletonBlock(
            modifier = Modifier.width(KoFestDimens.ThumbWidth),
            height = KoFestDimens.ThumbHeight,
        )
    }
}

/** 로딩 중임을 월 헤더 자리에 알린다. 스피너를 두지 않는다. */
@Composable
fun LoadingMonthHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.Paper)
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = strings().loading.default,
            style = KoFestTheme.type.monthNumeral,
            color = KoFestColors.Jaju,
            textAlign = TextAlign.Start,
        )
    }
}
