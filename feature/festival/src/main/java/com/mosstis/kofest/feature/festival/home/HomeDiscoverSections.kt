package com.mosstis.kofest.feature.festival.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.Place
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.domain.festival.model.Theme
import com.mosstis.kofest.feature.festival.common.FestivalImage
import java.time.format.DateTimeFormatter

/**
 * "지금 떠나기 좋은 곳" — 테마 캐러셀 (기획서 08).
 *
 * **자동으로 넘기지 않는다.** 배너는 훑는 것이라 넘어가도 되지만 테마는 읽는 것이라
 * 읽는 중에 넘어가면 짜증난다.
 */
@Composable
fun ThemeCarousel(
    themes: List<Theme>,
    expandedThemeId: Long?,
    onToggle: (Long) -> Unit,
    onItem: (Theme.Kind, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()

    Column(modifier = modifier) {
        BoxWithConstraints {
            // 카드 폭은 화면의 72%. 양옆이 살짝 보여야 더 있다는 것이 전달된다.
            val cardWidth = maxWidth * 0.72f
            LazyRow(
                state = state,
                contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(themes, key = { it.id }) { theme ->
                    ThemeCard(
                        theme = theme,
                        selected = theme.id == expandedThemeId,
                        onClick = { onToggle(theme.id) },
                        modifier = Modifier.width(cardWidth),
                    )
                }
            }
        }

        // 펼친 것은 카드 **아래**에 붙는다. 캐러셀은 그대로 둔다.
        val expanded = themes.firstOrNull { it.id == expandedThemeId }
        if (expanded != null) {
            ThemeExpanded(theme = expanded, onItem = onItem)
        }
    }
}

@Composable
private fun ThemeCard(
    theme: Theme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(20.dp))
            .background(KoFestColors.JajuDeep)
            .clickable(onClick = onClick)
            // 고른 카드가 100%, 나머지는 옅게. 지금 무엇을 펼쳤는지 알린다.
            .alpha(if (selected || theme.id == theme.id) 1f else 0.55f),
    ) {
        FestivalImage(
            url = theme.coverUrl,
            title = theme.title,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to KoFestColors.JajuDeep.copy(alpha = 0.86f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            theme.badge?.let { badge ->
                Text(
                    text = badge,
                    style = KoFestTheme.type.bannerKicker,
                    color = KoFestColors.BannerKicker,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = theme.title,
                style = KoFestTheme.type.bannerTitle,
                color = KoFestColors.OnJaju,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            theme.subtitle?.let { subtitle ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = KoFestTheme.type.bannerMeta,
                    color = KoFestColors.OnJaju.copy(alpha = 0.84f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = strings().theme.count.fill("n" to theme.count),
                style = KoFestTheme.type.cardMeta,
                color = KoFestColors.OnJaju.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * 펼친 테마. **번호를 매긴다 — 순서가 곧 동선이다.**
 * 담긴 것을 전부 보여준다. "더 보기"를 두면 결국 별도 화면이 필요해진다.
 */
@Composable
private fun ThemeExpanded(theme: Theme, onItem: (Theme.Kind, Long) -> Unit) {
    val s = strings()

    Column(
        modifier = Modifier
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 14.dp),
    ) {
        theme.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItem(item.kind, item.contentId) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${index + 1}",
                    style = KoFestTheme.type.monthNumeral.copy(fontSize = 15.sp),
                    color = KoFestColors.Muted,
                    modifier = Modifier.width(18.dp),
                )
                FestivalImage(
                    url = item.thumbUrl,
                    title = item.title,
                    hanjaSize = 17.sp,
                    modifier = Modifier
                        .size(width = 58.dp, height = 44.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = KoFestTheme.type.rowTitle.copy(fontSize = 13.5.sp),
                        color = KoFestColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val meta = listOfNotNull(
                        item.region,
                        item.period?.let { "${PERIOD.format(it.start)} – ${PERIOD.format(it.endInclusive)}" },
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(text = meta, style = KoFestTheme.type.cardMeta, color = KoFestColors.Muted)
                    }
                }
                // 그날뿐인 것(축제)과 언제든 갈 수 있는 곳(관광지)을 색으로 나눈다.
                Text(
                    text = if (item.kind == Theme.Kind.FESTIVAL) s.theme.kindFestival else s.theme.kindPlace,
                    style = KoFestTheme.type.badge,
                    color = if (item.kind == Theme.Kind.FESTIVAL) KoFestColors.OnJaju else KoFestColors.PlaceTagInk,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (item.kind == Theme.Kind.FESTIVAL) {
                                KoFestColors.Jaju
                            } else {
                                KoFestColors.PlaceTagBackground
                            },
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
            if (index != theme.items.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(KoFestDimens.HairlineThickness)
                        .background(KoFestColors.Line),
                )
            }
        }
    }
}

/** "가볼 만한 곳" — 관광지 8건. 썸네일이 가로형이다: 건물과 풍경은 가로가 자연스럽다 */
@Composable
fun PlaceCardRow(
    places: List<Place>,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = KoFestDimens.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(places, key = { it.contentId }) { place ->
            Column(
                modifier = Modifier
                    .width(168.dp)
                    .clickable { onClick(place.contentId) },
            ) {
                FestivalImage(
                    url = place.thumbUrl ?: place.imageUrl,
                    title = place.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.height(8.dp))
                strings().place.typeName(place.type)?.let { typeName ->
                    Text(text = typeName, style = KoFestTheme.type.badge, color = KoFestColors.Muted)
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = place.title,
                    style = KoFestTheme.type.cardTitle,
                    color = KoFestColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                place.district?.let {
                    Spacer(Modifier.height(3.dp))
                    Text(text = it, style = KoFestTheme.type.cardMeta, color = KoFestColors.Muted)
                }
            }
        }
    }
}

/** 홈 아래 매거진 최신 3건. 0건이면 이 섹션을 아예 부르지 않는다 */
@Composable
fun StoryPreviewList(
    stories: List<Story>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
        stories.forEach { story ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(story.slug) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FestivalImage(
                    url = story.coverUrl,
                    title = story.title,
                    hanjaSize = 20.sp,
                    modifier = Modifier
                        .size(width = 72.dp, height = 56.dp)
                        .clip(RoundedCornerShape(2.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.title,
                        style = KoFestTheme.type.rowTitle.copy(fontSize = 14.sp),
                        color = KoFestColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    story.excerpt?.let {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = it,
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
                    .height(KoFestDimens.HairlineThickness)
                    .background(KoFestColors.Line),
            )
        }
    }
}

private val PERIOD: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
