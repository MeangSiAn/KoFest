package com.mosstis.kofest.feature.festival.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons

/**
 * 검색 — 축제와 관광지를 함께.
 *
 * 종류를 나눠 보여주되 **한 화면 안에** 둔다. 사람은 "경복궁"을 칠 때
 * 그게 축제인지 관광지인지 생각하지 않는다.
 */
@Composable
fun SearchScreen(
    uiState: SearchContract.State,
    onAction: (SearchContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val focusRequester = remember { FocusRequester() }

    // 검색 화면은 글자를 치러 들어온 곳이다. 열자마자 커서를 둔다.
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        SearchBar(
            query = uiState.query,
            onQueryChange = { onAction(SearchContract.Action.QueryChanged(it)) },
            onBack = { onAction(SearchContract.Action.Back) },
            onClear = { onAction(SearchContract.Action.Clear) },
            focusRequester = focusRequester,
        )

        val result = uiState.result
        when {
            uiState.isIdle -> Unit

            result == null -> Unit

            result.allFailed -> EmptyState(
                title = s.error.loadFailed,
                body = s.error.loadFailedHint,
                actionLabel = null,
                onAction = {},
            )

            result.isEmpty -> EmptyState(
                title = s.empty.searchNoResult.fill("query" to result.query),
                body = s.empty.noResultHint,
                actionLabel = null,
                onAction = {},
            )

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                item(key = "head") {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = KoFestDimens.ScreenMargin)
                            .padding(top = 12.dp, bottom = 6.dp),
                    ) {
                        Text(text = result.query, style = KoFestTheme.type.screenTitle, color = KoFestColors.Ink)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(
                                s.list.count.fill("count" to result.festivals.size)
                                    .takeIf { result.festivals.isNotEmpty() },
                                s.place.count.fill("count" to result.places.size)
                                    .takeIf { result.places.isNotEmpty() },
                            ).joinToString(" · "),
                            style = KoFestTheme.type.rowPlace,
                            color = KoFestColors.Muted,
                        )
                    }
                }

                if (result.festivals.isNotEmpty()) {
                    item(key = "f-head") {
                        SectionHead(
                            title = s.list.tabFestival,
                            count = s.list.count.fill("count" to result.festivals.size),
                            onSeeAll = { onAction(SearchContract.Action.SeeAllFestivals) },
                        )
                    }
                    items(result.festivals, key = { "f-${it.contentId}" }) { festival ->
                        FestivalResultRow(
                            festival = festival,
                            language = uiState.language,
                            onClick = { onAction(SearchContract.Action.OpenFestival(festival.contentId)) },
                        )
                    }
                }

                if (result.places.isNotEmpty()) {
                    item(key = "p-head") {
                        SectionHead(
                            title = s.list.tabPlace,
                            count = s.place.count.fill("count" to result.places.size),
                            onSeeAll = null,
                        )
                    }
                    items(result.places, key = { "p-${it.contentId}" }) { place ->
                        PlaceResultRow(
                            place = place,
                            onClick = { onAction(SearchContract.Action.OpenPlace(place.contentId)) },
                        )
                    }
                }

                item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
) {
    val s = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = KoFestIcons.Back,
            contentDescription = s.action.back,
            tint = KoFestColors.Ink,
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onBack),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(2.dp))
                .background(KoFestColors.NoticeBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(text = s.action.search, style = KoFestTheme.type.rowTitle, color = KoFestColors.Muted)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = KoFestTheme.type.rowTitle.copy(color = KoFestColors.Ink),
                    cursorBrush = SolidColor(KoFestColors.Jaju),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = KoFestIcons.Close,
                    contentDescription = s.action.close,
                    tint = KoFestColors.Muted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onClear),
                )
            }
        }
    }
}

@Composable
private fun SectionHead(title: String, count: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 16.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = title, style = KoFestTheme.type.bodyStrong, color = KoFestColors.Ink)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = count, style = KoFestTheme.type.offline, color = KoFestColors.Muted)
            if (onSeeAll != null) {
                Text(
                    text = strings().place.seeAll,
                    style = KoFestTheme.type.offline,
                    color = KoFestColors.Jaju,
                    modifier = Modifier.clickable(onClick = onSeeAll),
                )
            }
        }
    }
}

/** 종류 꼬리표. 축제는 자주 채움, 관광지는 옅은 초록 — 한눈에 갈린다. */
@Composable
private fun KindTag(label: String, isFestival: Boolean) {
    Text(
        text = label,
        style = KoFestTheme.type.badge.copy(fontSize = 9.5.sp),
        color = if (isFestival) KoFestColors.OnJaju else KoFestColors.PlaceTagInk,
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (isFestival) KoFestColors.Jaju else KoFestColors.PlaceTagBackground)
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Composable
private fun ResultRow(
    tag: String,
    isFestival: Boolean,
    title: String,
    meta: String,
    imageUrl: String?,
    fallbackUrl: String?,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(66.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(KoFestColors.JajuDeep),
            ) {
                FestivalImage(
                    url = imageUrl,
                    fallbackUrl = fallbackUrl,
                    title = title,
                    modifier = Modifier.fillMaxSize(),
                    hanjaSize = 19.sp,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KindTag(label = tag, isFestival = isFestival)
                    Text(
                        text = title,
                        style = KoFestTheme.type.cardTitle,
                        color = KoFestColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = meta,
                        style = KoFestTheme.type.cardMeta,
                        color = KoFestColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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

@Composable
private fun FestivalResultRow(festival: Festival, language: AppLanguage, onClick: () -> Unit) {
    ResultRow(
        tag = strings().list.tabFestival,
        isFestival = true,
        title = festival.title,
        meta = listOf(festival.region, FestivalFormat.period(festival, language))
            .filter { it.isNotBlank() }
            .joinToString(" · "),
        imageUrl = festival.thumbUrl,
        fallbackUrl = null,
        onClick = onClick,
    )
}

@Composable
private fun PlaceResultRow(place: Place, onClick: () -> Unit) {
    ResultRow(
        tag = strings().list.tabPlace,
        isFestival = false,
        title = place.title,
        meta = place.fullAddress,
        imageUrl = place.thumbUrl,
        fallbackUrl = place.imageUrl,
        onClick = onClick,
    )
}
