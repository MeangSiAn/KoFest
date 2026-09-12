package com.mosstis.kofest.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme

/**
 * 기획서의 `.seg` — 자주 테두리 안에 칸이 붙어 있고 선택된 칸이 자주로 채워진다.
 *
 * 언어 전환과 목록·달력 전환이 같은 모양을 쓴다. 둘 다 "지금 무엇을 보고 있는가"를
 * 바꾸는 자리라, 다른 모양이면 사용자가 다른 종류의 동작으로 읽는다.
 */
@Composable
fun PillSegment(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, KoFestColors.Jaju, RoundedCornerShape(20.dp)),
    ) {
        labels.forEachIndexed { index, label ->
            SegmentButton(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = KoFestTheme.type.chip.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        ),
        color = if (selected) KoFestColors.OnJaju else KoFestColors.Jaju,
        modifier = Modifier
            .background(if (selected) KoFestColors.Jaju else Color.Transparent)
            .clickable(
                enabled = !selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 11.dp, vertical = 5.dp),
    )
}
