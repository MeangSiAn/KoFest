package com.mosstis.kofest.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * 기획서 `docs/03_기획_홈목록.md` / `KoFest_기획서.html` 의 색 토큰.
 *
 * 크림색(#F4F1EA)은 쓰지 않는다. 황토는 '진행중' 표시 한 곳에만 쓴다.
 */
object KoFestColors {
    /** 키컬러 */
    val Jaju = Color(0xFF6B1E32)

    /** 이미지 오버레이, 눌린 상태 */
    val JajuDeep = Color(0xFF3E1420)

    /** '진행중' 표시 전용 */
    val Hwangto = Color(0xFFA8783A)

    /** 배경 — 자주를 3% 섞은 흰색 */
    val Paper = Color(0xFFFCFAF9)

    /** 본문 */
    val Ink = Color(0xFF231C1E)

    /** 보조 텍스트 */
    val Muted = Color(0xFF8A7C7E)

    /** 구분선 */
    val Line = Color(0xFFEAE3E2)

    // 파생 토큰 — 기획서 목업에서 실제로 쓰인 값들
    val NoticeBackground = Color(0xFFF5EEEC)
    val NoticeInk = Color(0xFF6E5F60)
    val OfflineBackground = Color(0xFFF3EAD9)
    val OfflineInk = Color(0xFF7A5A14)
    val Skeleton = Color(0xFFEDE6E7)
    val BannerKicker = Color(0xFFE8CFA9)
    val NowDot = Color(0xFFE3B15E)

    /** 사진 없는 자리에 쓰는 자주색 면의 그라데이션 양 끝 */
    val BlankTop = Color(0xFF5C1B2C)
    val BlankBottom = Color(0xFF3E1420)

    /** 상세 본문. 목록 본문(Ink)보다 살짝 옅다 */
    val DetailBody = Color(0xFF3F3436)

    /** 상세 상단 오버레이 버튼 / 사진 카운터 */
    val Scrim = Color(0xFF231C1E)

    /** 지도 열기 박스 배경 */
    val MapBoxBackground = Color(0xFFF6F1F0)

    // ── 인트로 ──
    // 앱에서 유일하게 색을 가득 쓰는 화면. 여기서 색을 다 쓰고 그다음부터 흰 종이로 간다.
    val IntroGradientTop = Color(0xFF7A2338)
    val IntroGradientMid = Color(0xFF4A1522)
    val IntroGradientBottom = Color(0xFF2E0E18)

    // ── 달력 ──
    /** 이전·다음 달 날짜 숫자 */
    val CalendarOutDay = Color(0xFFD6CDCD)
    /** 오늘 칸 배경 */
    val CalendarToday = Color(0xFFF3EBEC)

    val OnJaju = Color(0xFFFFFFFF)

    // 테마에 담긴 항목의 종류 표. 축제는 자주로 채우고, 관광지는 이 초록을 쓴다 —
    // "언제든 갈 수 있는 곳"과 "그날뿐인 것"을 구분하기 위한 것이다 (기획서 08).
    val PlaceTagInk = Color(0xFF2C5138)
    val PlaceTagBackground = Color(0xFFEAF2EC)

    // 오늘 뭐하지 (기획서 11). 홈 위젯 바탕은 BlankTop→BlankBottom 을 그대로 쓴다
    /** 위젯 안 뒤집힌 카드 `linear-gradient(158deg, #7A2A40, #521A2A)` */
    val PickCardTop = Color(0xFF7A2A40)
    val PickCardBottom = Color(0xFF521A2A)
    /** 카드 위 폭죽 마크 */
    val PickMark = Color(0xFFE8B23C)
    /** 결과 카드 아래쪽 그늘 `rgba(20,8,12)` */
    val PickShade = Color(0xFF14080C)
    /** 결과 카드 유형 배지 바탕 `rgba(30,12,18,.55)` */
    val PickBadgeScrim = Color(0xFF1E0C12)
    /** 위치 거부 안내 글자색 */
    val PickDenied = Color(0xFF9A3232)
}
