package com.mosstis.kofest.feature.festival.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.component.NowMark
import com.mosstis.kofest.core.designsystem.component.PillSegment
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Banner
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.LanguageCounts
import com.mosstis.kofest.domain.festival.model.RegionBucket
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate

/** 헤더 — 로고 · 검색 · 언어 세그먼트. */
@Composable
fun HomeHeader(
    language: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    showSearch: Boolean = true,
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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = strings().app.name,
            style = KoFestTheme.type.wordmark,
            color = KoFestColors.Jaju,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showSearch) {
                Icon(
                    imageVector = KoFestIcons.Search,
                    contentDescription = strings().action.search,
                    tint = KoFestColors.Ink,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onSearch),
                )
            }
            LanguageSegment(language = language, onSelect = onSelectLanguage)
        }
    }
}

/**
 * 언어 전환. 밑줄 텍스트가 아니라 세그먼트다 —
 * 외국인이 첫 화면에서 가장 먼저 찾는 버튼이라 눌리는 것임이 보여야 한다.
 */
@Composable
fun LanguageSegment(
    language: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    PillSegment(
        labels = listOf("한국어", "EN"),
        selectedIndex = if (language == AppLanguage.KO) 0 else 1,
        onSelect = { index -> onSelect(if (index == 0) AppLanguage.KO else AppLanguage.EN) },
        modifier = modifier,
    )
}

/**
 * 배너 한 장. 250dp. 서버 관리자 배너를 그대로 그린다.
 *
 * 글이 하나도 없는 배너(이미지만)는 **어둡게 덮지 않는다** — 그라데이션은 글을 읽히게 하려고 있는 것이라
 * 글이 없으면 이미지를 가리기만 한다.
 */
@Composable
fun BannerCard(
    banner: Banner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(KoFestDimens.BannerHeight)
            .clip(RoundedCornerShape(2.dp))
            .background(KoFestColors.JajuDeep)
            .clickable(enabled = banner.link != null, onClick = onClick),
    ) {
        FestivalImage(
            url = banner.imageUrl,
            title = banner.title.orEmpty(),
            contentDescription = banner.imageAlt,
            modifier = Modifier.fillMaxSize(),
            hanjaSize = 64.sp,
        )

        if (!banner.hasText) return@Box

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to KoFestColors.JajuDeep.copy(alpha = 0.26f),
                    0.32f to KoFestColors.JajuDeep.copy(alpha = 0.04f),
                    1.0f to KoFestColors.JajuDeep.copy(alpha = 0.74f),
                ),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            if (!banner.kicker.isNullOrBlank()) {
                Text(text = banner.kicker!!, style = KoFestTheme.type.bannerKicker, color = KoFestColors.BannerKicker)
                Spacer(Modifier.height(9.dp))
                Box(
                    Modifier
                        .width(22.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.48f)),
                )
                Spacer(Modifier.height(11.dp))
            }
            if (!banner.title.isNullOrBlank()) {
                Text(
                    text = banner.title!!,
                    style = KoFestTheme.type.bannerTitle,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!banner.subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(9.dp))
                Text(
                    text = banner.subtitle!!,
                    style = KoFestTheme.type.bannerMeta,
                    color = Color.White.copy(alpha = 0.76f),
                )
            }
        }
    }
}

@Composable
fun BannerDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = KoFestDimens.ScreenMargin, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(if (index == selected) KoFestColors.Jaju else KoFestColors.Line),
            )
        }
    }
}

/** 섹션 제목 + 영문(또는 한글) 보조 + '전체 N'. */
@Composable
fun SectionHeader(
    title: String,
    sub: String,
    seeAllCount: Int?,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = title, style = KoFestTheme.type.sectionTitle, color = KoFestColors.Ink)
            Spacer(Modifier.height(5.dp))
            Text(text = sub, style = KoFestTheme.type.sectionSub, color = KoFestColors.Muted)
        }
        if (seeAllCount != null) {
            Text(
                text = strings().action.seeAll.fill("count" to seeAllCount),
                style = KoFestTheme.type.rowPlace,
                color = KoFestColors.Muted,
                modifier = Modifier
                    .clickable(onClick = onSeeAll)
                    .padding(bottom = 2.dp),
            )
        }
    }
}

/** 홈의 가로 카드. 하나하나를 골라야 하는 자리라 행이 아니라 카드다. */
@Composable
fun FestivalCard(
    festival: Festival,
    today: LocalDate,
    language: AppLanguage,
    showStateMark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateLabel = FestivalFormat.stateLabel(festival, today, strings())

    Column(
        modifier = modifier
            .width(KoFestDimens.CardWidth)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(2.dp))
                .background(KoFestColors.JajuDeep),
        ) {
            FestivalImage(
                url = festival.imageUrl,
                fallbackUrl = festival.thumbUrl,
                title = festival.title,
                modifier = Modifier.fillMaxSize(),
            )
            if (showStateMark && stateLabel != null) {
                NowMark(
                    label = stateLabel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(11.dp),
                )
            }
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
            text = festival.region,
            style = KoFestTheme.type.cardMeta,
            color = KoFestColors.Muted,
        )
        Text(
            text = FestivalFormat.period(festival, language),
            style = KoFestTheme.type.cardMeta,
            color = KoFestColors.Muted,
        )
    }
}

/**
 * 지역으로 찾기 — `/home` 의 `regions` 를 **오는 대로 전부** 그린다.
 *
 * 시도 단위다. 일정 짜기의 광역권 9개로 묶지 않는다 —
 * 목록 필터가 시도 단위라 묶으면 필터와 안 맞는다.
 * 정사각 타일 격자를 쓰지 않는다. 어디서나 보는 형태이고 화면의 1/3 을 먹는다.
 */
@Composable
fun RegionGrid(
    regions: List<RegionBucket>,
    totalCount: Int,
    onRegion: (String) -> Unit,
    onAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
        regions.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                pair.forEach { bucket ->
                    RegionRow(
                        bucket = bucket,
                        onClick = { onRegion(bucket.code) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KoFestColors.Line)
                .clickable(onClick = onAll)
                .padding(13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = strings().home.regionAll,
                style = KoFestTheme.type.button.copy(fontWeight = FontWeight.Medium),
                color = KoFestColors.Ink,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = totalCount.toString(),
                style = KoFestTheme.type.regionCount,
                color = KoFestColors.Jaju,
            )
        }
    }
}

/** 축제 건수를 크게 두고 관광지는 아래에 작게 적는다. */
@Composable
private fun RegionRow(
    bucket: RegionBucket,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = bucket.name,
                style = KoFestTheme.type.regionName,
                color = KoFestColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = bucket.count.toString(),
                    style = KoFestTheme.type.regionCount,
                    color = KoFestColors.Jaju,
                )
                if (bucket.places > 0) {
                    Text(
                        text = strings().place.count.fill("count" to bucket.places),
                        style = KoFestTheme.type.cardMeta,
                        color = KoFestColors.Muted,
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

/**
 * 언어 안내. 홈 최하단에 둔다 —
 * 스크롤을 끝까지 내린 사람은 콘텐츠를 다 본 상태라 "언어를 바꾸라"가 가장 잘 먹힌다.
 */
@Composable
fun LanguageNotice(
    language: AppLanguage,
    counts: LanguageCounts,
    onSwitch: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()

    Column(
        modifier = modifier
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(KoFestColors.NoticeBackground)
            .padding(20.dp),
    ) {
        Text(text = s.language.noticeTitle, style = KoFestTheme.type.bodyStrong, color = KoFestColors.Ink)
        Spacer(Modifier.height(4.dp))
        Text(
            text = s.language.notice.fill("ko" to counts.ko.festival, "en" to counts.en.festival),
            style = KoFestTheme.type.notice,
            color = KoFestColors.NoticeInk,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = s.language.noticeWhy, style = KoFestTheme.type.offline, color = KoFestColors.Muted)
        Spacer(Modifier.height(14.dp))

        val other = if (language == AppLanguage.KO) AppLanguage.EN else AppLanguage.KO
        Text(
            text = s.language.switch,
            style = KoFestTheme.type.button,
            color = KoFestColors.OnJaju,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(KoFestColors.Jaju)
                .clickable { onSwitch(other) }
                .padding(vertical = 11.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}


/**
 * 홈의 '오늘 뭐하지' 한 칸 (기획서 11 · `.pkbox`).
 *
 * 바탕을 자주 그라데이션으로 두어 주변 섹션과 다르게 — 눈에 걸려야 한다.
 * 왼쪽엔 뒤집힌 카드 두 장이 겹친 그림. 뽑는 것임을 알린다.
 */
@Composable
fun PickBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings()
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(KoFestColors.BlankTop, KoFestColors.BlankBottom),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset(1000f, 580f),
                ),
            )
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.width(75.dp).height(99.dp)) {
            // 뒤에 반쯤 비치는 두 번째 장
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .width(68.dp)
                    .height(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            )
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .width(68.dp)
                    .height(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(KoFestColors.PickCardTop, KoFestColors.PickCardBottom),
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset(370f, 1000f),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                com.mosstis.kofest.feature.festival.pick.FireworkMark(
                    size = 34.dp,
                    modifier = Modifier.alpha(0.85f),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = s.pick.title,
                style = KoFestTheme.type.pickBoxTitle,
                color = KoFestColors.Paper,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = s.pick.homeLead,
                style = KoFestTheme.type.pickBoxBody,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(11.dp))
            Text(
                text = s.pick.homeCta,
                style = KoFestTheme.type.pickBoxCta,
                color = KoFestColors.Jaju,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(KoFestColors.Paper)
                    .padding(horizontal = 18.dp, vertical = 9.dp),
            )
        }
    }
}
