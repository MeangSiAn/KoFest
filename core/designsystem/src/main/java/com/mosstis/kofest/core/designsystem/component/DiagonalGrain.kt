package com.mosstis.kofest.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * `-38deg` 방향으로 1px 선을 일정 간격 반복하는 결.
 *
 * 사진 없는 축제의 대체 면([BlankImage])과 인트로가 **같은 패턴**을 쓴다 —
 * 같은 세계라는 신호다. 값만 다르다 (대체 면 9dp/.028, 인트로 11dp/.03).
 *
 * CSS 대응: `repeating-linear-gradient(-38deg, rgba(255,255,255,a) 0 1px, transparent 1px Ndp)`
 */
fun DrawScope.drawDiagonalGrain(
    spacing: Dp,
    alpha: Float,
    color: Color = Color.White,
) {
    val strokeColor = color.copy(alpha = alpha)
    val stepPx = spacing.toPx()
    val diagonal = hypot(size.width, size.height)
    val dx = diagonal * COS_38
    val dy = diagonal * SIN_38

    var offset = -diagonal
    while (offset < diagonal * 2) {
        drawLine(
            color = strokeColor,
            start = Offset(offset, size.height + diagonal),
            end = Offset(offset + dx, size.height + diagonal - dy),
            strokeWidth = 1f,
            cap = StrokeCap.Butt,
        )
        offset += stepPx
    }
}

/** 사진 없는 면에서 쓰는 값 */
val BlankGrainSpacing: Dp = 9.dp
const val BlankGrainAlpha: Float = 0.028f

/** 인트로에서 쓰는 값 */
val IntroGrainSpacing: Dp = 11.dp
const val IntroGrainAlpha: Float = 0.03f

private const val COS_38 = 0.788f
private const val SIN_38 = 0.616f
