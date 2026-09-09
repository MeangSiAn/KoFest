package com.mosstis.kofest.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.mosstis.kofest.core.designsystem.theme.AppTheme
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestFonts

/**
 * 사진이 없을 때 쓰는 면.
 *
 * 회색 네모를 두지 않는다 — 고장난 것처럼 보인다.
 * 대각선 결이 들어간 자주색 면에 한자 한 글자를 불투명도 18% 로 얹는다.
 * 같은 자리에 같은 크기로 들어가므로 목록의 리듬이 깨지지 않는다.
 *
 * 기획서 CSS 대응:
 * ```
 * repeating-linear-gradient(-38deg, rgba(255,255,255,.028) 0 1px, transparent 1px 9px),
 * linear-gradient(158deg, #5C1B2C 0%, #3E1420 100%)
 * ```
 */
@Composable
fun BlankImage(
    hanja: String,
    modifier: Modifier = Modifier,
    hanjaSize: TextUnit = 42.sp,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // linear-gradient(158deg, ...) — CSS 각도는 위쪽 기준 시계방향이다.
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(KoFestColors.BlankTop, KoFestColors.BlankBottom),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.37f, size.height),
                ),
            )

            drawDiagonalGrain(spacing = BlankGrainSpacing, alpha = BlankGrainAlpha)
        }

        Text(
            text = hanja,
            color = Color.White.copy(alpha = 0.18f),
            fontFamily = KoFestFonts.NanumMyeongjo,
            fontSize = hanjaSize,
        )
    }
}

@Preview(widthDp = 168, heightDp = 210)
@Composable
private fun BlankImagePreview() {
    AppTheme {
        BlankImage(hanja = "燈", modifier = Modifier.fillMaxSize())
    }
}
