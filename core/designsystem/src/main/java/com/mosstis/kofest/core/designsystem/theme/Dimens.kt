package com.mosstis.kofest.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * 기획서의 치수. 좌우 여백은 [ScreenMargin] 하나로 통일한다 —
 * 헤더·섹션 제목·배너 첫 장·카드 첫 장·목록 행이 모두 같은 선에서 시작한다.
 */
object KoFestDimens {
    /** 좌우 여백. 모든 요소가 이 값 */
    val ScreenMargin = 22.dp

    /** 가로 스크롤 오른쪽 끝에서 다음 장이 보이는 폭 */
    val Peek = 30.dp

    val BannerHeight = 250.dp
    val BannerGap = 10.dp

    val CardWidth = 168.dp
    val CardGap = 12.dp

    /** 목록 행 왼쪽 날짜 열 */
    val DateColumnWidth = 30.dp
    val ThumbWidth = 74.dp
    val ThumbHeight = 92.dp

    // 상세
    val DetailHeroHeight = 300.dp
    val DetailGalleryItemWidth = 124.dp
    val DetailGalleryItemHeight = 92.dp
    val DetailMapBoxHeight = 130.dp
    val NearbyCardWidth = 150.dp

    /** 인트로만 좌우 여백이 26dp 다. 종이 화면(22dp)과 다른 세계라는 신호 */
    val IntroMargin = 26.dp

    val HairlineThickness = 1.dp
}
