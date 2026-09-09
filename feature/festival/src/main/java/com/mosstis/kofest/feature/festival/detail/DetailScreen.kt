package com.mosstis.kofest.feature.festival.detail

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.component.SkeletonBlock
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.DetailStatus
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import java.time.LocalDate

@Composable
fun DetailScreen(
    uiState: DetailContract.State,
    onAction: (DetailContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()
    val detail = uiState.detail

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        when {
            uiState.isLoading && detail == null -> DetailSkeleton()

            detail == null -> Column {
                Spacer(Modifier.statusBarsPadding())
                EmptyState(
                    title = if (uiState.hasFatalError) s.error.loadFailed else s.error.notFound,
                    body = s.error.loadFailedHint,
                    actionLabel = s.action.retry,
                    onAction = { onAction(DetailContract.Action.Retry) },
                )
            }

            else -> DetailContent(detail = detail, uiState = uiState, today = today, onAction = onAction)
        }

        // 데이터가 없는 상태에서도 뒤로 갈 수 있어야 한다.
        if (detail == null) {
            Icon(
                imageVector = KoFestIcons.Back,
                contentDescription = s.action.back,
                tint = KoFestColors.Ink,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 16.dp)
                    .size(21.dp)
                    .clickable { onAction(DetailContract.Action.Back) },
            )
        }
    }

    val viewerIndex = uiState.viewerIndex
    if (viewerIndex != null && uiState.photos.isNotEmpty()) {
        PhotoViewer(
            photos = uiState.photos.map { it.url },
            initialIndex = viewerIndex,
            onClose = { onAction(DetailContract.Action.ClosePhotoViewer) },
        )
    }
}

@Composable
private fun DetailContent(
    detail: FestivalDetail,
    uiState: DetailContract.State,
    today: LocalDate,
    onAction: (DetailContract.Action) -> Unit,
) {
    val s = strings()
    val photos = uiState.photos
    val pagerState = rememberPagerState(pageCount = { photos.size })

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "hero") {
            DetailHero(
                photos = photos,
                title = detail.festival.title,
                pagerState = pagerState,
                isSaved = uiState.isSaved,
                onBack = { onAction(DetailContract.Action.Back) },
                onToggleSaved = { onAction(DetailContract.Action.ToggleSaved) },
                onShare = { onAction(DetailContract.Action.Share) },
                onPhotoClick = { onAction(DetailContract.Action.OpenPhotoViewer(it)) },
            )
        }

        item(key = "head") {
            DetailHead(detail = detail, language = uiState.language, today = today)
            DetailActions(
                detail = detail,
                onDirections = { onAction(DetailContract.Action.OpenDirections) },
                onCall = { onAction(DetailContract.Action.Call) },
                onHomepage = { onAction(DetailContract.Action.OpenHomepage) },
            )
        }

        // ── 축제 소개 ──
        item(key = "overview") {
            val overview = detail.overview
            if (overview != null) {
                DetailSection(title = s.detail.overview) {
                    Text(
                        text = overview,
                        style = KoFestTheme.type.detailBody,
                        color = KoFestColors.DetailBody,
                        maxLines = if (uiState.overviewExpanded) Int.MAX_VALUE else OVERVIEW_COLLAPSED_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // 길이가 축제마다 편차가 크다. 4줄까지 보이고 더 보기로 펼친다.
                    if (overview.length > OVERVIEW_FOLD_THRESHOLD) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (uiState.overviewExpanded) s.detail.less else s.detail.more,
                            style = KoFestTheme.type.detailFold,
                            color = KoFestColors.Jaju,
                            modifier = Modifier.clickable { onAction(DetailContract.Action.ToggleOverview) },
                        )
                        Box(
                            Modifier
                                .padding(top = 2.dp)
                                .height(1.dp)
                                .background(KoFestColors.Line),
                        )
                    }
                }
            } else {
                // 왜 없는지 말한다. "아직 수집 안 된 것"과 "정보가 없는 것"을 구분한다.
                DetailSection(title = s.detail.overview) {
                    DetailNotice(
                        title = if (detail.detailStatus == DetailStatus.NONE) {
                            s.detail.noOverviewSoon
                        } else {
                            s.detail.noOverview
                        },
                        // 한국어 유도는 소개글이 비었을 때만 한다 — 매 화면에서 반복하면 잔소리다.
                        body = if (uiState.language == AppLanguage.EN) s.language.detailNotice else null,
                    )
                }
            }
        }

        // ── 안내 ──
        if (detail.info.isNotEmpty()) {
            item(key = "info") {
                DetailSection(title = s.detail.info) { DetailInfoTable(items = detail.info) }
            }
        }

        // ── 프로그램 (줄바꿈을 그대로 살린다 — 번호 목록이라 없으면 읽을 수 없다) ──
        val program = detail.program
        if (program != null) {
            item(key = "program") {
                DetailSection(title = s.detail.program) {
                    Text(text = program, style = KoFestTheme.type.detailBody, color = KoFestColors.DetailBody)
                }
            }
        }

        // ── 주최 · 주관 ──
        if (detail.hosts.isNotEmpty()) {
            item(key = "hosts") {
                DetailSection(title = s.detail.hosts) { DetailHosts(hosts = detail.hosts) }
            }
        }

        // ── 사진 (0장이면 섹션을 숨긴다) ──
        if (detail.photos.isNotEmpty()) {
            item(key = "photos") {
                DetailSection(title = s.detail.photos, horizontalPadding = false) {
                    DetailGallery(
                        photos = detail.photos,
                        title = detail.festival.title,
                        // 갤러리는 대표 이미지 다음부터이므로 뷰어 위치는 +1
                        onPhotoClick = { index ->
                            onAction(DetailContract.Action.OpenPhotoViewer(index + photos.size - detail.photos.size))
                        },
                    )
                }
            }
        }

        // ── 위치 ──
        if (detail.hasCoordinates) {
            item(key = "location") {
                DetailSection(title = s.detail.location) {
                    DetailMapBox(onClick = { onAction(DetailContract.Action.OpenDirections) })
                }
            }
        }

        // ── 주변 축제 ──
        if (detail.nearby.isNotEmpty()) {
            item(key = "nearby") {
                Column(modifier = Modifier.padding(top = 34.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KoFestDimens.ScreenMargin)
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = s.detail.nearby,
                            style = KoFestTheme.type.detailSectionTitle,
                            color = KoFestColors.Ink,
                        )
                        Text(
                            text = s.detail.nearbyRange,
                            style = KoFestTheme.type.offline,
                            color = KoFestColors.Muted,
                        )
                    }
                    DetailNearby(
                        festivals = detail.nearby,
                        language = uiState.language,
                        onClick = { onAction(DetailContract.Action.OpenNearby(it)) },
                    )
                }
            }
        }

        item(key = "bottom") {
            Spacer(
                Modifier
                    .height(32.dp)
                    .navigationBarsPadding(),
            )
        }
    }
}

/** 전체화면 사진 뷰어. 좌우로 넘기고 우상단 X 로 닫는다. */
@Composable
private fun PhotoViewer(
    photos: List<String>,
    initialIndex: Int,
    onClose: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, photos.lastIndex),
        pageCount = { photos.size },
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings().detail.photoCount.fill(
                        "current" to pagerState.currentPage + 1,
                        "total" to photos.size,
                    ),
                    style = KoFestTheme.type.photoCount,
                    color = Color.White,
                )
                Icon(
                    imageVector = KoFestIcons.Close,
                    contentDescription = strings().action.back,
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onClose),
                )
            }
        }
    }
}

/** 실제 배치 그대로의 뼈대. 히어로 자리부터 잡아 화면이 흔들리지 않게 한다. */
@Composable
private fun DetailSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = KoFestDimens.DetailHeroHeight)
        Column(modifier = Modifier.padding(KoFestDimens.ScreenMargin)) {
            SkeletonBlock(Modifier.fillMaxWidth(0.25f))
            Spacer(Modifier.height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.8f), height = 22.dp)
            Spacer(Modifier.height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.4f), height = 14.dp)
            Spacer(Modifier.height(9.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.55f))
            Spacer(Modifier.height(34.dp))
            repeat(4) {
                SkeletonBlock(Modifier.fillMaxWidth())
                Spacer(Modifier.height(11.dp))
            }
        }
    }
}

private const val OVERVIEW_COLLAPSED_LINES = 4

/** 이보다 짧으면 4줄 안에 들어와 '더 보기'가 의미 없다 (한 줄 ≈ 30자) */
private const val OVERVIEW_FOLD_THRESHOLD = 110
