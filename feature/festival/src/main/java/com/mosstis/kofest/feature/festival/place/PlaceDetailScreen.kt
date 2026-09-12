package com.mosstis.kofest.feature.festival.place

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import com.mosstis.kofest.feature.festival.detail.DetailSection

/**
 * 관광지 상세 (기획서 03).
 *
 * 축제 상세에서 날짜·프로그램·주최를 뺀 형태다. 비어 있는 섹션을 숨기는 규칙이 이미 있으므로
 * 없는 항목은 저절로 빠진다.
 */
@Composable
fun PlaceDetailScreen(
    uiState: PlaceDetailContract.State,
    onAction: (PlaceDetailContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val detail = uiState.detail

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = KoFestIcons.ChevronLeft,
                contentDescription = s.action.back,
                tint = KoFestColors.Ink,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onAction(PlaceDetailContract.Action.Back) },
            )
        }

        when {
            uiState.isLoading -> Spacer(Modifier.height(1.dp))

            uiState.notReady -> EmptyState(
                title = s.place.comingSoonRegion,
                body = s.place.sub,
                actionLabel = null,
                onAction = {},
            )

            uiState.hasError || detail == null -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = s.action.retry,
                onAction = { onAction(PlaceDetailContract.Action.Retry) },
            )

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                val place = detail.place

                item(key = "hero") {
                    FestivalImage(
                        url = place.imageUrl ?: place.thumbUrl,
                        title = place.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f),
                    )
                }

                item(key = "head") {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = KoFestDimens.ScreenMargin)
                            .padding(top = 20.dp),
                    ) {
                        s.place.typeName(place.type)?.let { typeName ->
                            Text(
                                text = typeName,
                                style = KoFestTheme.type.badge,
                                color = KoFestColors.Jaju,
                            )
                            Spacer(Modifier.height(7.dp))
                        }
                        Text(
                            text = place.title,
                            style = KoFestTheme.type.screenTitle,
                            color = KoFestColors.Ink,
                        )
                        if (place.fullAddress.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = place.fullAddress,
                                style = KoFestTheme.type.rowPlace,
                                color = KoFestColors.Muted,
                            )
                        }
                    }
                }

                item(key = "actions") { PlaceActions(place = place, onAction = onAction) }

                item(key = "overview") {
                    DetailSection(title = s.detail.place) {
                        Text(
                            // detail 은 지금 항상 null 이다. 왜 없는지 말하고 주소·지도로 대신한다.
                            text = detail.overview ?: s.place.noOverview,
                            style = KoFestTheme.type.rowPlace,
                            color = if (detail.overview != null) KoFestColors.Ink else KoFestColors.Muted,
                        )
                    }
                }


                if (detail.nearby.isNotEmpty()) {
                    item(key = "nearby") {
                        DetailSection(title = s.place.nearby, horizontalPadding = false) {
                            Column {
                                detail.nearby.forEach { nearby ->
                                    NearbyRow(
                                        place = nearby,
                                        onClick = { onAction(PlaceDetailContract.Action.OpenNearby(nearby.contentId)) },
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "bottom") { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

/** 누른 다음 아무 일도 안 일어나는 버튼을 두지 않는다 — 값이 없으면 버튼을 뺀다 */
@Composable
private fun PlaceActions(place: Place, onAction: (PlaceDetailContract.Action) -> Unit) {
    val s = strings()
    val actions = buildList {
        if (place.latitude != null && place.longitude != null) {
            add(Triple(KoFestIcons.Directions, s.detail.directions) {
                onAction(PlaceDetailContract.Action.OpenDirections)
            })
        }
        if (place.tel != null) {
            add(Triple(KoFestIcons.Call, s.action.call) { onAction(PlaceDetailContract.Action.Call) })
        }
    }
    if (actions.isEmpty()) return

    val single = actions.size == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { (icon, label, onClick) ->
            Column(
                modifier = Modifier
                    // 버튼 한 개만 남으면 가로 폭을 채우지 않고 왼쪽 정렬한다.
                    .then(if (single) Modifier.width(110.dp) else Modifier.weight(1f))
                    .clip(RoundedCornerShape(2.dp))
                    .border(1.dp, KoFestColors.Line, RoundedCornerShape(2.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KoFestColors.Jaju,
                    modifier = Modifier.size(18.dp),
                )
                Text(text = label, style = KoFestTheme.type.detailAction, color = KoFestColors.Ink)
            }
        }
    }
}

@Composable
private fun NearbyRow(place: Place, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FestivalImage(
            url = place.thumbUrl,
            title = place.title,
            hanjaSize = 20.sp,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(2.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.title, style = KoFestTheme.type.rowTitle, color = KoFestColors.Ink)
            place.district?.let {
                Spacer(Modifier.height(3.dp))
                Text(text = it, style = KoFestTheme.type.rowWhen, color = KoFestColors.Muted)
            }
        }
    }
}
