package com.mosstis.kofest.feature.festival.common

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * 기획서 목업의 SVG path 를 그대로 옮긴 아이콘.
 *
 * Material 아이콘을 쓰지 않는 이유는 기획서 아이콘이 stroke 1.4~1.5 의 가는 선이고,
 * Material 채움 아이콘을 섞으면 화면의 결이 달라지기 때문이다.
 */
object KoFestIcons {

    val Home: ImageVector = stroked("M4 10.5L12 4l8 6.5M6 9.6V20h12V9.6")

    val List: ImageVector = stroked("M4 6.5h16M4 12h16M4 17.5h10")

    val Calendar: ImageVector = stroked(
        "M5.5 5.5h13a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1 -1.5 1.5h-13" +
            "a1.5 1.5 0 0 1 -1.5 -1.5v-12a1.5 1.5 0 0 1 1.5 -1.5z" +
            "M4 10h16M8.5 3.5v4M15.5 3.5v4",
    )

    val Saved: ImageVector =
        stroked("M12 20S4.5 15 4.5 9.8A4.1 4.1 0 0 1 12 8a4.1 4.1 0 0 1 7.5 1.8C19.5 15 12 20 12 20z")

    val Search: ImageVector = stroked(
        "M17.6 11a6.6 6.6 0 1 1 -13.2 0a6.6 6.6 0 1 1 13.2 0M16 16l4.5 4.5",
        strokeWidth = 1.5f,
    )

    val Back: ImageVector = stroked("M14.5 4.5L7 12l7.5 7.5", strokeWidth = 1.5f)

    val ChevronDown: ImageVector = stroked(
        pathData = "M1 1l4 4 4-4",
        strokeWidth = 1.7f,
        viewportWidth = 10f,
        viewportHeight = 6f,
        widthDp = 10f,
        heightDp = 6f,
    )

    /** 저장됨 — 같은 하트를 채워서 쓴다 */
    val SavedFilled: ImageVector = filled(
        "M12 20S4.5 15 4.5 9.8A4.1 4.1 0 0 1 12 8a4.1 4.1 0 0 1 7.5 1.8C19.5 15 12 20 12 20z",
    )

    val Share: ImageVector = stroked(
        "M19.6 6a2.6 2.6 0 1 1 -5.2 0a2.6 2.6 0 1 1 5.2 0" +
            "M9.6 12a2.6 2.6 0 1 1 -5.2 0a2.6 2.6 0 1 1 5.2 0" +
            "M19.6 18a2.6 2.6 0 1 1 -5.2 0a2.6 2.6 0 1 1 5.2 0" +
            "M9.3 10.8l5.4-3.2M9.3 13.2l5.4 3.2",
        strokeWidth = 1.6f,
    )

    /** 길찾기 — 지도 핀 */
    val Directions: ImageVector = stroked(
        "M12 21s7-5.6 7-11a7 7 0 1 0 -14 0c0 5.4 7 11 7 11z" +
            "M14.4 10a2.4 2.4 0 1 1 -4.8 0a2.4 2.4 0 1 1 4.8 0",
        strokeWidth = 1.5f,
    )

    val Call: ImageVector = stroked(
        "M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a1 1 0 0 1 -1 1A16 16 0 0 1 4 5a1 1 0 0 1 1-1z",
        strokeWidth = 1.5f,
    )

    val Homepage: ImageVector = stroked(
        "M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7M12 15V3M8 7l4-4 4 4",
        strokeWidth = 1.5f,
    )

    val ChevronLeft: ImageVector = stroked("M14.5 5L8 12l6.5 7", strokeWidth = 1.8f)

    val ChevronRight: ImageVector = stroked("M9.5 5L16 12l-6.5 7", strokeWidth = 1.8f)

    /** 설정 행 오른쪽의 작은 화살표 (viewBox 0 0 8 14) */
    val ChevronRightSmall: ImageVector = stroked(
        pathData = "M1 1l6 6-6 6", strokeWidth = 1.5f,
        viewportWidth = 8f, viewportHeight = 14f, widthDp = 8f, heightDp = 14f,
    )

    /** MY 탭 — 사람 */
    val My: ImageVector = stroked(
        "M15.6 8.5a3.6 3.6 0 1 1 -7.2 0a3.6 3.6 0 1 1 7.2 0M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7",
    )

    val Close: ImageVector = stroked("M6 6l12 12M18 6L6 18", strokeWidth = 1.6f)

    private fun filled(pathData: String): ImageVector = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = SolidColor(androidx.compose.ui.graphics.Color.Black),
    ).build()

    private fun stroked(
        pathData: String,
        strokeWidth: Float = 1.4f,
        viewportWidth: Float = 24f,
        viewportHeight: Float = 24f,
        widthDp: Float = 24f,
        heightDp: Float = 24f,
    ): ImageVector = ImageVector.Builder(
        defaultWidth = widthDp.dp,
        defaultHeight = heightDp.dp,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = null,
        stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}
