package com.mosstis.kofest.feature.festival.my

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.fill
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.domain.festival.model.SavedFestival
import com.mosstis.kofest.feature.festival.common.EmptyState
import com.mosstis.kofest.feature.festival.common.FestivalFormat
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import com.mosstis.kofest.feature.festival.common.KoFestLinks
import com.mosstis.kofest.feature.festival.home.HomeHeader
import com.mosstis.kofest.feature.festival.list.FestivalRow
import com.mosstis.kofest.feature.festival.list.FestivalRowSkeleton
import java.time.LocalDate
import java.util.Locale

/**
 * MY — 저장한 축제와 설정. 계정이 없으므로 "내 정보"가 아니라 "내 기기의 것"이다.
 * 설치 직후 이 화면은 반드시 비어 있다. 빈 화면을 잘 만드는 것이 이 탭의 핵심이다.
 */
@Composable
fun MyScreen(
    uiState: MyContract.State,
    versionName: String,
    onAction: (MyContract.Action) -> Unit,
    /** 상단 검색 아이콘. 검색은 탭이 아니라 검색창에서 들어간다 */
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val s = strings()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        HomeHeader(
            language = uiState.language,
            onSelectLanguage = { onAction(MyContract.Action.SelectLanguage(it)) },
            onSearch = onSearch,
            showSearch = false,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "head") {
                Text(
                    text = s.my.title,
                    style = KoFestTheme.type.myTitle,
                    color = KoFestColors.Ink,
                    modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin).padding(top = 8.dp, bottom = 22.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KoFestDimens.ScreenMargin)
                        .padding(bottom = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                ) {
                    CountBlock(uiState.upcoming.size + uiState.past.size, s.my.savedCount)
                    // 저장이 쌓이면 대부분 지난 축제가 된다. 지금 갈 수 있는 게 몇 개인지가 실제로 궁금한 숫자다.
                    CountBlock(uiState.upcoming.size, s.my.upcomingCount)
                }
                Hairline()
            }

            when {
                uiState.isLoading -> items(count = 2, key = { "skeleton-$it" }) { FestivalRowSkeleton() }

                uiState.isEmpty -> item(key = "empty") {
                    EmptyState(
                        title = s.my.emptyTitle,
                        body = s.my.emptyHint,
                        actionLabel = s.my.emptyCta,
                        onAction = { onAction(MyContract.Action.BrowseFestivals) },
                    )
                }

                else -> {
                    item(key = "saved-head") { SectionHead(s.my.savedList, s.my.savedOrder) }
                    items(uiState.upcoming, key = { "up-" + it.key }) { saved ->
                        SwipeToUnsaveRow(saved = saved, today = today, onAction = onAction)
                    }
                    if (uiState.past.isNotEmpty()) {
                        item(key = "past-toggle") {
                            PastToggle(
                                count = uiState.past.size,
                                expanded = uiState.pastExpanded,
                                onClick = { onAction(MyContract.Action.TogglePast) },
                            )
                        }
                        if (uiState.pastExpanded) {
                            items(uiState.past, key = { "past-" + it.key }) { saved ->
                                SwipeToUnsaveRow(saved = saved, today = today, onAction = onAction, past = true)
                            }
                        }
                    }
                }
            }

            // ── 설정 ──
            item(key = "settings") {
                SectionHead(s.my.settings, null, topPadding = 32.dp)
                SettingRow(
                    label = s.my.language,
                    value = if (uiState.language == AppLanguage.KO) "한국어" else "English",
                    onClick = { onAction(MyContract.Action.OpenLanguageDialog) },
                )
                SettingRow(label = s.my.notify, value = s.my.notifyOff, onClick = { onAction(MyContract.Action.NotifyTap) })
            }

            // ── 약관 · 정책 (스토어 심사가 확인하는 항목) ──
            item(key = "terms") {
                SectionHead(s.my.terms, null, topPadding = 30.dp)
                SettingRow(label = s.my.privacy, onClick = { onAction(MyContract.Action.OpenLink(KoFestLinks.PRIVACY)) })
                SettingRow(label = s.my.tos, onClick = { onAction(MyContract.Action.OpenLink(KoFestLinks.TERMS)) })
                SettingRow(
                    label = s.my.copyright,
                    value = s.my.dataSourceVal,
                    onClick = { onAction(MyContract.Action.OpenLink(KoFestLinks.COPYRIGHT)) },
                )
                SettingRow(label = s.my.license, onClick = { onAction(MyContract.Action.OpenLink(KoFestLinks.LICENSES)) }, divider = false)
            }

            // ── 앱 정보 ──
            item(key = "about") {
                SectionHead(s.my.about, null, topPadding = 30.dp)
                SettingRow(
                    label = s.my.clearCache,
                    value = uiState.cacheBytes?.let(::formatBytes),
                    onClick = { onAction(MyContract.Action.ClearCacheTap) },
                )
                SettingRow(
                    label = s.my.version,
                    // 홈을 아직 못 불렀으면 서버 설정이 없어 '최신'으로 보인다.
                    // 모르는 상태에서 "업데이트 있음"을 띄우는 것보다 낫다.
                    value = "$versionName · " +
                        if (uiState.updateAvailable) s.my.versionUpdate else s.my.versionLatest,
                    chevron = false,
                    divider = false,
                )
                Spacer(Modifier.height(22.dp))
                Text(
                    text = s.my.localOnly,
                    style = KoFestTheme.type.myNote,
                    color = KoFestColors.Muted,
                    modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin).padding(bottom = 30.dp),
                )
            }
        }
    }

    if (uiState.showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { onAction(MyContract.Action.DismissDialogs) },
            containerColor = KoFestColors.Paper,
            title = { Text(s.my.language, style = KoFestTheme.type.mySection, color = KoFestColors.Ink) },
            text = {
                Column {
                    DialogOption("한국어", uiState.language == AppLanguage.KO) { onAction(MyContract.Action.SelectLanguage(AppLanguage.KO)) }
                    DialogOption("English", uiState.language == AppLanguage.EN) { onAction(MyContract.Action.SelectLanguage(AppLanguage.EN)) }
                }
            },
            confirmButton = {},
        )
    }

    if (uiState.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { onAction(MyContract.Action.DismissDialogs) },
            containerColor = KoFestColors.Paper,
            title = { Text(s.my.clearCache, style = KoFestTheme.type.mySection, color = KoFestColors.Ink) },
            text = {
                Text(
                    text = s.my.clearCacheAsk.fill("size" to formatBytes(uiState.cacheBytes ?: 0L)),
                    style = KoFestTheme.type.body,
                    color = KoFestColors.Ink,
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(MyContract.Action.ConfirmClearCache) }) {
                    Text(s.my.clearCache, color = KoFestColors.Jaju, style = KoFestTheme.type.button)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(MyContract.Action.DismissDialogs) }) {
                    Text(s.action.back, color = KoFestColors.Muted, style = KoFestTheme.type.button)
                }
            },
        )
    }
}

@Composable
private fun CountBlock(count: Int, label: String) {
    Column {
        Text(text = count.toString(), style = KoFestTheme.type.myCountNumber, color = KoFestColors.Jaju)
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = KoFestTheme.type.myCountLabel, color = KoFestColors.Muted)
    }
}

@Composable
private fun SectionHead(title: String, sub: String?, topPadding: androidx.compose.ui.unit.Dp = 24.dp) {
    Column(
        modifier = Modifier
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = topPadding, bottom = 12.dp),
    ) {
        Text(text = title, style = KoFestTheme.type.mySection, color = KoFestColors.Ink)
        if (sub != null) {
            Spacer(Modifier.height(3.dp))
            Text(text = sub, style = KoFestTheme.type.mySectionSub, color = KoFestColors.Muted)
        }
    }
}

/** 왼쪽으로 밀면 저장 해제. */
@Composable
private fun SwipeToUnsaveRow(
    saved: SavedFestival,
    today: LocalDate,
    onAction: (MyContract.Action) -> Unit,
    past: Boolean = false,
) {
    val s = strings()
    val state = rememberSwipeToDismissBoxState()
    val currentOnAction by rememberUpdatedState(onAction)
    // 끝까지 밀려 안착한 순간에만 지운다. (confirmValueChange 는 deprecated)
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            currentOnAction(MyContract.Action.Unsave(saved.language, saved.festival))
        }
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KoFestColors.Jaju)
                    .padding(end = KoFestDimens.ScreenMargin),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(text = s.my.unsave, style = KoFestTheme.type.button, color = KoFestColors.OnJaju)
            }
        },
    ) {
        Box(Modifier.background(KoFestColors.Paper)) {
            FestivalRow(
                festival = saved.festival,
                today = today,
                language = saved.language,
                onClick = { onAction(MyContract.Action.OpenFestival(saved.language, saved.festival.contentId)) },
                whenText = if (past) "${s.state.ended} · ${FestivalFormat.period(saved.festival, saved.language)}" else null,
                dimmed = past,
            )
        }
    }
}

/** "지난 축제 N ──── 펼치기". 끝난 축제는 지우지 않고 아래로 접는다. */
@Composable
private fun PastToggle(count: Int, expanded: Boolean, onClick: () -> Unit) {
    val s = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KoFestDimens.ScreenMargin)
            .padding(top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(text = s.my.pastToggle.fill("count" to count), style = KoFestTheme.type.rowPlace, color = KoFestColors.Muted)
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(KoFestColors.Line),
        )
        Text(text = if (expanded) s.my.collapse else s.my.expand, style = KoFestTheme.type.offline, color = KoFestColors.Jaju)
    }
}

@Composable
private fun SettingRow(
    label: String,
    value: String? = null,
    chevron: Boolean = true,
    divider: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(horizontal = KoFestDimens.ScreenMargin)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = KoFestTheme.type.myRow, color = KoFestColors.Ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (value != null) Text(text = value, style = KoFestTheme.type.myRowValue, color = KoFestColors.Muted)
                if (chevron) {
                    Icon(
                        imageVector = KoFestIcons.ChevronRightSmall,
                        contentDescription = null,
                        tint = KoFestColors.Muted,
                        modifier = Modifier.width(8.dp).height(14.dp),
                    )
                }
            }
        }
        if (divider) Hairline()
    }
}

@Composable
private fun DialogOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = KoFestTheme.type.myRow.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
        color = if (selected) KoFestColors.Jaju else KoFestColors.Ink,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun Hairline() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KoFestColors.Line),
    )
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1) String.format(Locale.US, "%.1fMB", mb) else String.format(Locale.US, "%dKB", bytes / 1024)
}
