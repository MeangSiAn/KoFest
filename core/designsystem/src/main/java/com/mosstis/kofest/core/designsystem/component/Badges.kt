package com.mosstis.kofest.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme

/**
 * 카드 이미지 좌상단의 진행 상태 배지.
 *
 * 기획서는 backdrop-blur 를 쓰지만 Compose 기본 API 로는 API 31+ 라 반투명 자주로 대체한다.
 * 실제 데이터에는 흰 바탕 포스터가 많아, 기획서 목업의 0.55 로는 흰 글씨가 읽히지 않는다.
 * blur 가 없는 만큼 불투명도를 올려 같은 대비를 만든다.
 */
@Composable
fun NowMark(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(KoFestColors.JajuDeep.copy(alpha = 0.78f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(KoFestColors.NowDot),
        )
        Text(
            text = label,
            style = KoFestTheme.type.badge,
            color = Color.White,
        )
    }
}

/** 목록 행의 '진행중' 표시. 황토는 여기에만 쓴다. */
@Composable
fun OnNowLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(KoFestColors.Hwangto),
        )
        Text(
            text = label,
            style = KoFestTheme.type.rowWhen.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = KoFestColors.Hwangto,
        )
    }
}
