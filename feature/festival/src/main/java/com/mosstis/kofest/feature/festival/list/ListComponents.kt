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
import com.mosstis.kofest.core.designsystem.component.PillSegment
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalState
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate

@Composable
fun ListHeader(
    title: String,
    mode: ListContract.Mode,
    onSelectMode: (ListContract.Mode) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    /** 관광 탭에서는 false. 관광지에는 기간이 없어 달력에 찍을 것이 없다 */
    showModeSegment: Boolean = true,
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = KoFestTheme.type.screenTitle,
            color = KoFestColors.Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // 달력은 탭이 아니라 이 화면의 다른 보기다 (기획서 05). 언어 전환과 같은 세그먼트를 쓴다.
        if (showModeSegment) {
            PillSegment(
                labels = listOf(strings().nav.list, strings().nav.calendar),
                selectedIndex = if (mode == ListContract.Mode.LIST) 0 else 1,
                onSelect = { index ->
                    onSelectMode(if (index == 0) ListContract.Mode.LIST else ListContract.Mode.CALENDAR)
                },
            )
        }
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
    /** 관광 탭에서는 false — 관광지에는 기간이 없다 */
    showPeriodFilters: Boolean = true,
    /** 관광 탭의 유형 칩. 축제 탭에는 없다 */
    typeLabel: String? = null,
    typeSelected: Boolean = false,
    onType: () -> Unit = {},
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
            if (showPeriodFilters) {
                FilterChip(periodLabel, periodSelected, hasMenu = true, onClick = onPeriod)
                FilterChip(s.filter.ongoing, ongoingSelected, hasMenu = false, onClick = onOngoing)
            } else if (typeLabel != null) {
                FilterChip(typeLabel, typeSelected, hasMenu = true, onClick = onType)
            }
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

/**
 * 축제 | 관광 전환. 건수를 함께 보여준다 — 기획서 03 의 "903 / 4,120".
 *
 * 관광지 건수는 서버가 목록 응답에 담아 줄 때만 안다. 모르면 숫자를 감춘다 (0 으로 쓰지 않는다).
 */
@Composable
fun ListTabs(
    tab: ListContract.Tab,
    festivalCount: Int?,
    placeCount: Int?,
    onSelect: (ListContract.Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ListTab(s.list.tabFestival, festivalCount, tab == ListContract.Tab.FESTIVAL) {
            onSelect(ListContract.Tab.FESTIVAL)
        }
        ListTab(s.list.tabPlace, placeCount, tab == ListContract.Tab.PLACE) {
            onSelect(ListContract.Tab.PLACE)
        }
    }
}

@Composable
private fun ListTab(label: String, count: Int?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = KoFestTheme.type.sectionTitle.copy(fontSize = 16.sp),
            color = if (selected) KoFestColors.Ink else KoFestColors.Muted,
        )
        if (count != null) {
            Text(
                text = count.toString(),
                style = KoFestTheme.type.regionCount,
                color = if (selected) KoFestColors.Jaju else KoFestColors.Muted,
            )
        }
    }
}

/** 관광지 한 줄. 날짜 열이 없고 **썸네일이 가로형**이다 — 건물과 풍경은 가로가 자연스럽다 */
@Composable
fun PlaceRow(place: Place, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val s = strings()
    Column(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                s.place.typeName(place.type)?.let { typeName ->
                    Text(text = typeName, style = KoFestTheme.type.badge, color = KoFestColors.Muted)
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = place.title,
                    style = KoFestTheme.type.rowTitle,
                    color = KoFestColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                // 기간이 없으므로 날짜 줄 자리를 주소가 쓴다.
                val address = listOfNotNull(place.district, place.address).joinToString(" ")
                if (address.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = address,
                        style = KoFestTheme.type.rowPlace,
                        color = KoFestColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FestivalImage(
                url = place.thumbUrl ?: place.imageUrl,
                title = place.title,
                hanjaSize = 24.sp,
                modifier = Modifier
                    .size(width = 104.dp, height = 78.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(KoFestDimens.HairlineThickness)
                .background(KoFestColors.Line),
        )
    }
}

/** 관광 탭의 지역 헤더. 축제의 월 헤더와 같은 자리를 쓴다 */
@Composable
fun PlaceRegionHeader(region: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.Paper)
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 22.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = region, style = KoFestTheme.type.sectionTitle, color = KoFestColors.Ink)
        Text(
            text = count.toString(),
            style = KoFestTheme.type.regionCount,
            color = KoFestColors.Jaju,
        )
    }
}
