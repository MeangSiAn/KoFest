package com.mosstis.kofest.feature.festival.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.feature.festival.common.KoFestIcons
import com.mosstis.kofest.feature.festival.navigation.KoFestDestination

/**
 * 하단 탭. Material3 `NavigationBar` 를 쓰지 않는다 —
 * indicator pill 과 기본 높이가 기획서의 얇은 선 스타일과 맞지 않는다.
 */
@Composable
fun KoFestBottomBar(
    current: KoFestDestination,
    onSelect: (KoFestDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = strings().nav

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.Paper),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(KoFestDimens.HairlineThickness)
                .background(KoFestColors.Line),
        )

        // 높이를 Row 에 주고 탭이 그 높이를 꽉 채우게 한다.
        // 패딩으로 띄우면 패딩 부분이 눌리지 않는 죽은 띠가 되어, 아이콘 아래를 누르면 아무 일도 안 일어난다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(BAR_HEIGHT),
        ) {
            Tab(KoFestDestination.HOME, KoFestIcons.Home, labels.home, current, onSelect, Modifier.weight(1f))
            Tab(KoFestDestination.LIST, KoFestIcons.List, labels.list, current, onSelect, Modifier.weight(1f))
            Tab(KoFestDestination.PLAN, KoFestIcons.Plan, labels.plan, current, onSelect, Modifier.weight(1f))
            Tab(KoFestDestination.MAGAZINE, KoFestIcons.Magazine, labels.story, current, onSelect, Modifier.weight(1f))
            Tab(KoFestDestination.MY, KoFestIcons.My, labels.saved, current, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Tab(
    destination: KoFestDestination,
    icon: ImageVector,
    label: String,
    current: KoFestDestination,
    onSelect: (KoFestDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = destination == current
    val tint = if (selected) KoFestColors.Jaju else KoFestColors.Muted

    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = { onSelect(destination) },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            // 탭이 다섯이라 기획서가 아이콘을 19dp 로 줄였다. 더 늘릴 자리가 없으므로 탭을 더 만들지 않는다.
            modifier = Modifier.size(19.dp),
        )
        Text(
            text = label,
            style = KoFestTheme.type.tabLabel,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 기획서의 상하 여백(11 + 12dp)과 아이콘·라벨 높이를 합친 값.
 * 이 높이 전체가 눌린다 — Android 최소 터치 영역 48dp 를 넘긴다.
 */
private val BAR_HEIGHT = 60.dp
