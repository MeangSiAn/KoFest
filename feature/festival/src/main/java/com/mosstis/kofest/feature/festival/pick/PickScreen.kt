package com.mosstis.kofest.feature.festival.pick

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.PickedPlace
import com.mosstis.kofest.feature.festival.common.FestivalImage
import com.mosstis.kofest.feature.festival.home.HomeHeader
import java.util.Locale

/**
 * 오늘 뭐하지 (기획서 11). 위치를 묻는 안내와 카드 화면이 한 Composable 의 [PickContract.Phase] 다.
 *
 * 목업에 검색 아이콘이 없다 — 검색은 여기서 거쳐 가는 자리가 아니다.
 */
@Composable
fun PickScreen(
    uiState: PickContract.State,
    onAction: (PickContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(PickContract.Action.SelectLanguage(it)) },
            onSearch = {},
            showSearch = false,
        )

        when (uiState.phase) {
            PickContract.Phase.ASK -> AskPhase(uiState = uiState, onAction = onAction)
            PickContract.Phase.STAGE -> StagePhase(uiState = uiState, onAction = onAction)
        }
    }
}

// ─── 위치를 주기 전 ────────────────────────────────────────────────────────────

@Composable
private fun AskPhase(
    uiState: PickContract.State,
    onAction: (PickContract.Action) -> Unit,
) {
    val s = strings()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 44.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 점 셋 — 가운데만 자주. "여러 개 중 하나를 뽑는다" 는 뜻
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Box(
                    Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(if (index == 1) KoFestColors.Jaju else KoFestColors.Jaju.copy(alpha = 0.22f)),
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = s.pick.title,
            style = KoFestTheme.type.pickTitle,
            color = KoFestColors.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = s.pick.askLead + "\n" + s.pick.askBody,
            style = KoFestTheme.type.pickLead,
            color = KoFestColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(26.dp))
        PickButton(
            label = s.pick.allow,
            onClick = { onAction(PickContract.Action.RequestLocation) },
        )
        Spacer(Modifier.height(14.dp))
        // 거부해도 막다른 길로 두지 않는다. 안내를 바꾸고 "지역으로" 를 남긴다.
        Text(
            text = if (uiState.permissionDenied) s.pick.denied else s.pick.note,
            style = KoFestTheme.type.pickNote,
            color = if (uiState.permissionDenied) KoFestColors.PickDenied else KoFestColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = s.pick.byRegion,
            style = KoFestTheme.type.button,
            color = KoFestColors.Jaju,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onAction(PickContract.Action.BrowseByRegion) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

// ─── 뽑은 뒤 ──────────────────────────────────────────────────────────────────

@Composable
private fun StagePhase(
    uiState: PickContract.State,
    onAction: (PickContract.Action) -> Unit,
) {
    val s = strings()
    val picked = uiState.picked
    val context = LocalContext.current

    // 결과는 0.3초면 오는데 카드는 2초 뒤에 뒤집힌다. 그 사이에 사진을 받아 둔다 —
    // 원본이 600KB 라 느린 회선에서는 5초씩 걸려 뒤집힌 카드가 빈 면으로 남는다 (실기기).
    LaunchedEffect(picked?.place?.imageUrl) {
        val url = picked?.place?.imageUrl ?: return@LaunchedEffect
        SingletonImageLoader.get(context).enqueue(ImageRequest.Builder(context).data(url).build())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // "10km 안 24곳 중 3번째". 뽑기 전에는 빈 줄로 자리만 지킨다 — 카드가 뛰지 않게
        Text(
            text = when {
                uiState.isLocating -> s.pick.locating
                picked != null && !uiState.isDrawing -> s.pick.count.fill(
                    "km" to uiState.km,
                    "total" to uiState.total,
                    "nth" to uiState.seen,
                )
                else -> ""
            },
            style = KoFestTheme.type.pickCount,
            color = KoFestColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, bottom = 14.dp),
        )

        PickCard(
            picked = picked,
            isDrawing = uiState.isDrawing,
            modifier = Modifier
                .padding(horizontal = KoFestDimens.ScreenMargin)
                .fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            PickButton(
                label = when {
                    uiState.isDrawing -> s.pick.drawing
                    uiState.exhausted -> s.pick.reset
                    picked != null -> s.pick.again
                    else -> s.pick.draw
                },
                enabled = uiState.canDraw,
                onClick = { onAction(PickContract.Action.Draw) },
            )
            // '여기로 갈래요' 는 뽑힌 곳이 있을 때만 만든다. 눌러도 아무 일 없는 버튼을 두지 않는다.
            if (picked != null && !uiState.isDrawing) {
                PickButton(
                    label = s.pick.go,
                    ghost = true,
                    onClick = { onAction(PickContract.Action.Go) },
                )
            }
        }

        val message = when (val m = uiState.message) {
            null -> if (picked == null && !uiState.isDrawing && !uiState.isLocating) s.pick.tapToDraw else ""
            is PickContract.Message.NoneInRange -> s.pick.none.fill("km" to m.km)
            is PickContract.Message.AllSeen -> s.pick.all.fill("total" to m.total)
            PickContract.Message.Failed -> s.pick.fail
            PickContract.Message.LocationFailed -> s.pick.locationFailed
        }
        Text(
            text = message,
            style = KoFestTheme.type.pickNote,
            color = KoFestColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = KoFestDimens.ScreenMargin)
                .padding(top = 14.dp, bottom = 24.dp),
        )
    }
}

/**
 * 카드. 뽑는 중에는 돌고, 결과가 오면 뒤집히며 앞면(사진)이 나온다.
 *
 * `rotationY` 90° 를 지나는 순간 뒷면↔앞면을 바꾼다. 앞면은 미리 180° 돌려 두어 거울상이 되지 않게 한다.
 */
@Composable
private fun PickCard(
    picked: PickedPlace?,
    isDrawing: Boolean,
    modifier: Modifier = Modifier,
) {
    val settled = animateFloatAsState(
        targetValue = if (picked != null && !isDrawing) 180f else 0f,
        animationSpec = tween(durationMillis = 620),
        label = "flip",
    )
    val spin = rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing), RepeatMode.Restart),
        label = "spinAngle",
    )
    // 각도는 그리기 단계에서만 읽는다. 컴포지션에서 읽으면 도는 동안 매 프레임 다시 그리고,
    // 그 안의 이미지 요청까지 흔들린다.
    val showFront = picked != null && !isDrawing && settled.value > 90f

    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .graphicsLayer {
                rotationY = if (isDrawing) spin.value else settled.value
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .background(KoFestColors.JajuDeep),
    ) {
        if (showFront && picked != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                CardFront(picked)
            }
        } else {
            CardBack(showMark = true)
        }
    }
}

@Composable
private fun BoxScope.CardFront(picked: PickedPlace) {
    val s = strings()
    val place = picked.place

    // 썸네일(300×200)을 먼저 깔고 원본을 위에 얹는다. 원본이 올 때까지 빈 면이 아니라 흐린 사진이 보인다.
    FestivalImage(
        url = place.thumbUrl,
        fallbackUrl = place.imageUrl,
        title = place.title,
        hanjaSize = KoFestTheme.type.pickTitle.fontSize * 2.4,
        modifier = Modifier.fillMaxSize(),
    )
    place.imageUrl?.let { url ->
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
    // 아래쪽 그늘 — 사진이 밝아도 흰 글씨가 읽히게
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.36f to KoFestColors.PickShade.copy(alpha = 0.34f),
                    1f to KoFestColors.PickShade.copy(alpha = 0.82f),
                ),
            ),
    )
    place.typeName?.let { type ->
        Text(
            text = type,
            style = KoFestTheme.type.pickBadge,
            color = KoFestColors.OnJaju,
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(KoFestColors.PickBadgeScrim.copy(alpha = 0.55f))
                .padding(horizontal = 11.dp, vertical = 5.dp),
        )
    }
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp),
    ) {
        Text(
            text = place.title,
            style = KoFestTheme.type.pickCardTitle,
            color = KoFestColors.OnJaju,
        )
        Spacer(Modifier.height(7.dp))
        // 한국어 주소는 시도로 시작한다("경기도 용인시 …"). 앞에 지역을 또 붙이면 "경기도 · 경기도 …" 가 된다.
        val address = place.address
        val region = place.region
        val placeLine = when {
            address == null -> region.orEmpty()
            region == null || address.startsWith(region) -> address
            else -> "$region · $address"
        }
        Text(
            text = placeLine,
            style = KoFestTheme.type.pickCardPlace,
            color = KoFestColors.OnJaju.copy(alpha = 0.84f),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = s.pick.away.fill("km" to String.format(Locale.ROOT, "%.1f", picked.km)),
            style = KoFestTheme.type.pickCardKm,
            color = KoFestColors.OnJaju.copy(alpha = 0.7f),
        )
    }
}

/** 뒤집힌 카드. 자주 그라데이션에 폭죽 마크 하나 */
@Composable
private fun CardBack(showMark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(KoFestColors.PickCardTop, KoFestColors.PickCardBottom),
                    start = Offset.Zero,
                    end = Offset(0.37f * 1000f, 1000f),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showMark) FireworkMark(size = 58.dp, modifier = Modifier.alpha(0.85f))
    }
}

/**
 * 폭죽 마크 — 기획서의 SVG 를 그대로 옮긴 것. 가운데 원과 여섯 방향의 짧은 획.
 */
@Composable
fun FireworkMark(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = KoFestColors.PickMark,
) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.minDimension / 64f
        val stroke = 6f * unit
        val center = Offset(32f * unit, 32f * unit)
        drawCircle(color = color, radius = 9f * unit, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
        val rays = listOf(
            Offset(32f, 10f) to Offset(32f, 18f),
            Offset(32f, 54f) to Offset(32f, 46f),
            Offset(15f, 22f) to Offset(21f, 26f),
            Offset(49f, 42f) to Offset(43f, 38f),
            Offset(15f, 42f) to Offset(21f, 38f),
            Offset(49f, 22f) to Offset(43f, 26f),
        )
        rays.forEach { (from, to) ->
            drawLine(
                color = color,
                start = Offset(from.x * unit, from.y * unit),
                end = Offset(to.x * unit, to.y * unit),
                strokeWidth = stroke,
                cap = StrokeCap.Butt,
            )
        }
    }
}

/** 알약 버튼. 채움(자주) 또는 테두리(ghost) */
@Composable
private fun PickButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ghost: Boolean = false,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(30.dp)
    Text(
        text = label,
        style = KoFestTheme.type.pickButton,
        color = if (ghost) KoFestColors.Jaju else KoFestColors.OnJaju,
        textAlign = TextAlign.Center,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.55f)
            .clip(shape)
            .background(if (ghost) KoFestColors.Paper else KoFestColors.Jaju)
            .then(if (ghost) Modifier.border(1.dp, KoFestColors.Jaju, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 15.dp),
    )
}
