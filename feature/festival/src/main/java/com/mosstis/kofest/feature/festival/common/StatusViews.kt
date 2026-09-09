package com.mosstis.kofest.feature.festival.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme

/**
 * 네트워크 실패 안내 띠.
 *
 * 목록을 비우지 않는다. 마지막으로 받은 데이터를 그대로 두고 갱신 시각만 알린다.
 * 축제 정보는 하루 단위로 갱신되므로 어제 것을 보여줘도 대부분 맞다.
 */
@Composable
fun OfflineBand(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KoFestColors.OfflineBackground)
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(KoFestColors.Hwangto),
        )
        Text(text = message, style = KoFestTheme.type.offline, color = KoFestColors.OfflineInk)
    }
}

/**
 * 빈 화면.
 *
 * 사과하는 자리가 아니라 **다음 행동을 권하는 자리**다.
 * 왜 비었는지 말하고 버튼은 하나만 둔다 — 선택지가 많으면 아무것도 안 누른다.
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    actionLabel: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KoFestDimens.ScreenMargin, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(KoFestColors.Jaju),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "祭", style = KoFestTheme.type.seal, color = KoFestColors.OnJaju)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = KoFestTheme.type.emptyTitle,
            color = KoFestColors.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = body,
            style = KoFestTheme.type.emptyBody,
            color = KoFestColors.Muted,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null) {
            Spacer(Modifier.height(22.dp))
            Text(
                text = actionLabel,
                style = KoFestTheme.type.button,
                color = KoFestColors.Jaju,
                modifier = Modifier
                    .border(1.dp, KoFestColors.Jaju)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 22.dp, vertical = 11.dp),
            )
        }
    }
}
