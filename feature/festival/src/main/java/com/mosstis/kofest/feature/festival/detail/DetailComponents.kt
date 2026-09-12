package com.mosstis.kofest.feature.festival.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.FestivalPhoto
import com.mosstis.kofest.domain.festival.model.Host
import com.mosstis.kofest.domain.festival.model.InfoItem
import com.mosstis.kofest.domain.festival.model.InfoKey
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate

/**
 * 대표 이미지. 사진이 여러 장이면 좌우로 넘기고 우하단에 `1 / 8` 을 띄운다.
 * 상단 바는 히어로 안에 있어 스크롤하면 같이 올라간다 (기획서 목업 그대로).
 */
@Composable
fun DetailHero(
    photos: List<FestivalPhoto>,
    title: String,
    pagerState: PagerState,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onShare: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(KoFestDimens.DetailHeroHeight)
            .background(KoFestColors.JajuDeep),
    ) {
        if (photos.isEmpty()) {
            FestivalImage(
                url = null,
                title = title,
                modifier = Modifier.fillMaxSize(),
                hanjaSize = 76.sp,
            )
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                FestivalImage(
                    url = photos[page].url,
                    title = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPhotoClick(page) },
                    hanjaSize = 76.sp,
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to KoFestColors.JajuDeep.copy(alpha = 0.34f),
                    0.40f to KoFestColors.JajuDeep.copy(alpha = 0.02f),
                    1.0f to KoFestColors.JajuDeep.copy(alpha = 0.55f),
                ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ScrimIconButton(KoFestIcons.Back, strings().action.back, onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScrimIconButton(
                    icon = if (isSaved) KoFestIcons.SavedFilled else KoFestIcons.Saved,
                    contentDescription = if (isSaved) strings().action.saved else strings().action.save,
                    onClick = onToggleSaved,
                )
                ScrimIconButton(KoFestIcons.Share, strings().action.share, onShare)
            }
        }

        if (photos.size > 1) {
            Text(
                text = strings().detail.photoCount.fill(
                    "current" to pagerState.currentPage + 1,
                    "total" to photos.size,
                ),
                style = KoFestTheme.type.photoCount,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = KoFestDimens.ScreenMargin, bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KoFestColors.Scrim.copy(alpha = 0.44f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun ScrimIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(KoFestColors.Scrim.copy(alpha = 0.34f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(17.dp),
        )
    }
}

/** 상태 · 제목 · 기간 · 장소. */
@Composable
fun DetailHead(
    detail: FestivalDetail,
    language: AppLanguage,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val festival = detail.festival
    val stateLabel = FestivalFormat.detailStateLabel(festival, today, strings())
    val place = listOfNotNull(
        festival.region.takeIf { it.isNotBlank() },
        detail.address?.takeIf { it != festival.region },
    ).joinToString(" · ")

    Column(
        modifier = modifier.padding(
            start = KoFestDimens.ScreenMargin,
            end = KoFestDimens.ScreenMargin,
            top = 22.dp,
        ),
    ) {
        if (stateLabel != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(KoFestColors.Hwangto),
                )
                Text(
                    text = stateLabel,
                    style = KoFestTheme.type.detailState,
                    color = KoFestColors.Hwangto,
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        Text(text = festival.title, style = KoFestTheme.type.detailTitle, color = KoFestColors.Ink)
        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = FestivalFormat.period(festival, language),
                style = KoFestTheme.type.detailPeriod,
                color = KoFestColors.Jaju,
            )
            Text(
                text = FestivalFormat.duration(festival, strings()),
                style = KoFestTheme.type.detailPeriodSub,
                color = KoFestColors.Muted,
            )
        }

        if (place.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text(text = place, style = KoFestTheme.type.detailPlace, color = KoFestColors.Muted)
        }
    }
}

/**
 * 액션 줄. 쓸 수 있는 버튼만 만든다.
 * 눌러도 아무 일 없는 버튼은 없는 버튼보다 나쁘다 — 회색으로 남기지 않는다.
 * 하나만 남으면 가로 폭을 채우지 않고 왼쪽에 둔다.
 */
@Composable
fun DetailActions(
    detail: FestivalDetail,
    onDirections: () -> Unit,
    onCall: () -> Unit,
    onHomepage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val actions = buildList {
        if (detail.hasCoordinates) add(Triple(KoFestIcons.Directions, s.detail.directions, onDirections))
        if (detail.tel != null) add(Triple(KoFestIcons.Call, s.action.call, onCall))
        if (detail.homepageUrl != null) add(Triple(KoFestIcons.Homepage, s.detail.homepage, onHomepage))
    }
    if (actions.isEmpty()) return

    val single = actions.size == 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { (icon, label, onClick) ->
            Column(
                modifier = Modifier
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

/** 섹션 껍데기. 내용이 없으면 **제목까지 통째로** 부르지 않는다. */
@Composable
fun DetailSection(
    title: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(top = 34.dp)) {
        Text(
            text = title,
            style = KoFestTheme.type.detailSectionTitle,
            color = KoFestColors.Ink,
            modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin),
        )
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = if (horizontalPadding) {
                Modifier.padding(horizontal = KoFestDimens.ScreenMargin)
            } else {
                Modifier
            },
        ) {
            content()
        }
    }
}

/** 안내 표. 온 항목만 행을 만든다. */
@Composable
fun DetailInfoTable(items: List<InfoItem>, modifier: Modifier = Modifier) {
    val s = strings()

    Column(modifier = modifier) {
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = s.detail.labelFor(item.key),
                    style = KoFestTheme.type.detailInfoKey,
                    color = KoFestColors.Muted,
                    modifier = Modifier
                        .width(74.dp)
                        .padding(end = 14.dp, top = 10.dp, bottom = 10.dp),
                )
                Text(
                    text = item.value,
                    style = KoFestTheme.type.detailInfoValue,
                    color = KoFestColors.Ink,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(KoFestColors.Line),
            )
        }
    }
}

@Composable
fun DetailHosts(hosts: List<Host>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        hosts.forEach { host ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = host.name,
                    style = KoFestTheme.type.detailHost,
                    color = KoFestColors.Ink,
                    modifier = Modifier.weight(1f, fill = false),
                )
                val tel = host.tel
                if (tel != null) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = tel,
                        style = KoFestTheme.type.detailHostTel,
                        color = KoFestColors.Muted,
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
}

@Composable
fun DetailGallery(
    photos: List<FestivalPhoto>,
    title: String,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.url }) { index, photo ->
            Box(
                modifier = Modifier
                    .width(KoFestDimens.DetailGalleryItemWidth)
                    .height(KoFestDimens.DetailGalleryItemHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KoFestColors.JajuDeep)
                    .clickable { onPhotoClick(index) },
            ) {
                FestivalImage(
                    url = photo.thumbUrl,
                    title = title,
                    modifier = Modifier.fillMaxSize(),
                    hanjaSize = 26.sp,
                )
            }
        }
    }
}


/**
 * 주변 축제. 이미 가진 데이터로 만들 수 있고 API 호출이 0 이라
 * 비용 없이 체류시간을 늘린다.
 */
@Composable
fun DetailNearby(
    festivals: List<Festival>,
    language: AppLanguage,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(KoFestDimens.CardGap),
    ) {
        items(festivals, key = { it.contentId }) { festival ->
            Column(
                modifier = Modifier
                    .width(KoFestDimens.NearbyCardWidth)
                    .clickable { onClick(festival.contentId) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(KoFestDimens.NearbyCardWidth * 5 / 4)
                        .clip(RoundedCornerShape(2.dp))
                        .background(KoFestColors.JajuDeep),
                ) {
                    FestivalImage(
                        url = festival.imageUrl,
                        fallbackUrl = festival.thumbUrl,
                        title = festival.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = festival.title,
                    style = KoFestTheme.type.cardTitle,
                    color = KoFestColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = listOf(festival.region, FestivalFormat.period(festival, language))
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = KoFestTheme.type.cardMeta,
                    color = KoFestColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** 소개글이 없을 때의 안내. 왜 없는지 말하고, 한국어에 더 있다는 것을 알린다. */
@Composable
fun DetailNotice(
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(KoFestColors.NoticeBackground)
            .padding(18.dp),
    ) {
        Text(text = title, style = KoFestTheme.type.bodyStrong, color = KoFestColors.Ink)
        if (body != null) {
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = KoFestTheme.type.notice, color = KoFestColors.NoticeInk)
        }
    }
}

/** i18n 의 안내 항목 라벨. */
private fun com.mosstis.kofest.core.ui.strings.KoFestStrings.Detail.labelFor(key: InfoKey): String =
    when (key) {
        InfoKey.PLACE -> place
        InfoKey.PLAY_TIME -> playTime
        InfoKey.FEE -> fee
        InfoKey.DISCOUNT -> discount
        InfoKey.SPEND_TIME -> spendTime
        InfoKey.AGE_LIMIT -> ageLimit
        InfoKey.BOOKING -> booking
        InfoKey.PLACE_INFO -> placeInfo
        InfoKey.SUB_EVENT -> subEvent
        InfoKey.GRADE -> grade
    }
