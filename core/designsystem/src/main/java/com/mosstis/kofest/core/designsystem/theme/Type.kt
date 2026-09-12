package com.mosstis.kofest.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 기획서의 서체 3종.
 *
 * TODO(font): 아직 실제 폰트 파일이 저장소에 없어 시스템 폴백을 쓴다.
 *  실제 서체를 넣으려면 `core/designsystem/src/main/res/font/` 에 파일을 넣고
 *  아래 세 val 만 `FontFamily(Font(R.font.xxx, FontWeight.Normal), ...)` 로 바꾸면 된다.
 *  호출부는 전부 이 세 val 만 참조하므로 다른 파일은 건드릴 필요가 없다.
 *
 *  - Pretendard        → pretendard_regular / medium / semibold / bold
 *  - 나눔명조           → nanum_myeongjo_regular / bold / extrabold
 *  - Cormorant Garamond → cormorant_garamond_regular / semibold / italic
 */
object KoFestFonts {
    /** 본문·UI 전부. 한글과 영문이 같은 굵기로 보이는 것이 중요하다 */
    val Pretendard: FontFamily = FontFamily.SansSerif

    /** 한국어 화면의 제목과 배너 문구 */
    val NanumMyeongjo: FontFamily = FontFamily.Serif

    /** 숫자, 월 이름, 영문 보조 텍스트, 영어 화면의 제목 */
    val CormorantGaramond: FontFamily = FontFamily.Serif
}

/**
 * 기획서 목업의 px 값을 그대로 옮긴 스타일 모음.
 *
 * [screenTitle] / [sectionSub] 는 **언어에 따라 서체가 뒤집힌다.**
 * 한국어 화면은 명조가 제목이고 영문이 보조, 영어 화면은 반대다.
 * 인스턴스는 [koFestTypographyFor] 로 만들고 `LocalKoFestTypography` 로 읽는다.
 */
data class KoFestTypography(
    val wordmark: TextStyle,
    val screenTitle: TextStyle,
    val sectionTitle: TextStyle,
    val sectionSub: TextStyle,
    val bannerKicker: TextStyle,
    val bannerTitle: TextStyle,
    val bannerMeta: TextStyle,
    val cardTitle: TextStyle,
    val cardMeta: TextStyle,
    val badge: TextStyle,
    val monthNumeral: TextStyle,
    val monthSub: TextStyle,
    val dateNumeral: TextStyle,
    val dateDayOfWeek: TextStyle,
    val rowTitle: TextStyle,
    val rowPlace: TextStyle,
    val rowWhen: TextStyle,
    val regionName: TextStyle,
    val regionCount: TextStyle,
    val chip: TextStyle,
    val tabLabel: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val button: TextStyle,
    val emptyTitle: TextStyle,
    val emptyBody: TextStyle,
    val notice: TextStyle,
    val offline: TextStyle,
    val seal: TextStyle,
    // 상세
    val detailTitle: TextStyle,
    val detailState: TextStyle,
    val detailPeriod: TextStyle,
    val detailPeriodSub: TextStyle,
    val detailPlace: TextStyle,
    val detailSectionTitle: TextStyle,
    val detailBody: TextStyle,
    val detailFold: TextStyle,
    val detailInfoKey: TextStyle,
    val detailInfoValue: TextStyle,
    val detailHost: TextStyle,
    val detailHostTel: TextStyle,
    val detailAction: TextStyle,
    val photoCount: TextStyle,
    // 인트로
    val introMark: TextStyle,
    val introHeadline: TextStyle,
    val introSub: TextStyle,
    val introFoot: TextStyle,
    val introKicker: TextStyle,
    val introTitle: TextStyle,
    val introDesc: TextStyle,
    val introOptionName: TextStyle,
    val introOptionDesc: TextStyle,
    val introOptionCount: TextStyle,
    val introNote: TextStyle,
    val introCta: TextStyle,
    val introSkip: TextStyle,
    // 달력
    val calMonth: TextStyle,
    val calMonthSub: TextStyle,
    val calWeekday: TextStyle,
    val calDay: TextStyle,
    val calSelected: TextStyle,
    val calSelectedSub: TextStyle,
    val calEmpty: TextStyle,
    // MY
    val myTitle: TextStyle,
    val myCountNumber: TextStyle,
    val myCountLabel: TextStyle,
    val mySection: TextStyle,
    val mySectionSub: TextStyle,
    val myRow: TextStyle,
    val myRowValue: TextStyle,
    val myNote: TextStyle,
    // 오늘 뭐하지 (기획서 11)
    val pickBoxTitle: TextStyle,
    val pickBoxBody: TextStyle,
    val pickBoxCta: TextStyle,
    val pickTitle: TextStyle,
    val pickLead: TextStyle,
    val pickButton: TextStyle,
    val pickNote: TextStyle,
    val pickCount: TextStyle,
    val pickCardTitle: TextStyle,
    val pickCardPlace: TextStyle,
    val pickCardKm: TextStyle,
    val pickBadge: TextStyle,
)

private val TrimNone = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun sans(
    size: Double,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: Double = size * 1.5,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = KoFestFonts.Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = TrimNone,
)

private fun serif(
    family: FontFamily,
    size: Double,
    weight: FontWeight = FontWeight.Normal,
    italic: Boolean = false,
    lineHeight: Double = size * 1.4,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    lineHeightStyle = TrimNone,
)

/**
 * @param koreanFirst 한국어 화면이면 true. 제목이 명조, 보조가 Cormorant 가 된다.
 */
fun koFestTypographyFor(koreanFirst: Boolean): KoFestTypography {
    val titleFamily = if (koreanFirst) KoFestFonts.NanumMyeongjo else KoFestFonts.CormorantGaramond
    val subFamily = if (koreanFirst) KoFestFonts.CormorantGaramond else KoFestFonts.NanumMyeongjo

    return KoFestTypography(
        wordmark = serif(KoFestFonts.CormorantGaramond, 23.0, FontWeight.SemiBold)
            .copy(letterSpacing = 0.1.em),
        screenTitle = serif(titleFamily, if (koreanFirst) 18.0 else 21.0, FontWeight.Bold),
        sectionTitle = serif(titleFamily, 19.0, FontWeight.Bold),
        sectionSub = serif(subFamily, 13.5, italic = koreanFirst),
        bannerKicker = serif(KoFestFonts.CormorantGaramond, 13.5, italic = true),
        bannerTitle = serif(titleFamily, 24.0, FontWeight.Bold, lineHeight = 24.0 * 1.32),
        bannerMeta = sans(11.5),
        cardTitle = sans(14.0, FontWeight.SemiBold, lineHeight = 14.0 * 1.4),
        cardMeta = sans(11.5, lineHeight = 11.5 * 1.6),
        badge = sans(10.0, FontWeight.SemiBold),
        monthNumeral = serif(KoFestFonts.CormorantGaramond, 23.0, FontWeight.SemiBold),
        monthSub = sans(11.5),
        dateNumeral = serif(KoFestFonts.CormorantGaramond, 23.0, FontWeight.SemiBold, lineHeight = 23.0),
        dateDayOfWeek = serif(KoFestFonts.CormorantGaramond, 11.0, italic = true, lineHeight = 11.0),
        rowTitle = sans(15.5, FontWeight.SemiBold, lineHeight = 15.5 * 1.4),
        rowPlace = sans(12.5),
        rowWhen = sans(11.5),
        regionName = sans(14.0, FontWeight.Medium),
        regionCount = serif(KoFestFonts.CormorantGaramond, 14.0),
        chip = sans(12.5, FontWeight.Medium),
        tabLabel = sans(10.0, lineHeight = 12.0),
        body = sans(13.0, lineHeight = 13.0 * 1.75),
        bodyStrong = sans(13.0, FontWeight.SemiBold, lineHeight = 13.0 * 1.75),
        button = sans(13.5, FontWeight.SemiBold),
        emptyTitle = sans(16.0, FontWeight.SemiBold),
        emptyBody = sans(13.0, lineHeight = 13.0 * 1.7),
        notice = sans(13.0, lineHeight = 13.0 * 1.75),
        offline = sans(12.0),
        seal = serif(KoFestFonts.NanumMyeongjo, 21.0),
        detailTitle = serif(titleFamily, if (koreanFirst) 23.0 else 26.0, FontWeight.Bold, lineHeight = 23.0 * 1.36),
        detailState = sans(11.5, FontWeight.SemiBold),
        detailPeriod = serif(KoFestFonts.CormorantGaramond, 16.0, FontWeight.SemiBold),
        detailPeriodSub = sans(12.5),
        detailPlace = sans(13.0),
        detailSectionTitle = serif(titleFamily, if (koreanFirst) 16.0 else 18.0, FontWeight.Bold),
        detailBody = sans(13.5, lineHeight = 13.5 * 1.78),
        detailFold = sans(12.5),
        detailInfoKey = sans(12.5, FontWeight.Medium),
        detailInfoValue = sans(13.0, lineHeight = 13.0 * 1.6),
        detailHost = sans(13.0),
        detailHostTel = sans(12.0),
        detailAction = sans(11.0),
        photoCount = serif(KoFestFonts.CormorantGaramond, 12.0),
        // 인트로는 언어를 묻기 전이라 서체 역할을 뒤집지 않는다. 항상 명조 제목 + Cormorant 보조.
        introMark = serif(KoFestFonts.CormorantGaramond, 44.0, FontWeight.SemiBold)
            .copy(letterSpacing = 0.14.em),
        introHeadline = serif(KoFestFonts.NanumMyeongjo, 25.0, FontWeight.Bold, lineHeight = 25.0 * 1.44),
        introSub = sans(13.0, lineHeight = 13.0 * 1.7),
        introFoot = sans(11.0).copy(letterSpacing = 0.02.em),
        introKicker = serif(KoFestFonts.CormorantGaramond, 15.0, italic = true),
        introTitle = serif(KoFestFonts.NanumMyeongjo, 23.0, FontWeight.Bold, lineHeight = 23.0 * 1.4),
        introDesc = sans(12.5, lineHeight = 12.5 * 1.68),
        introOptionName = sans(16.0, FontWeight.SemiBold),
        introOptionDesc = sans(11.5),
        introOptionCount = serif(KoFestFonts.CormorantGaramond, 17.0),
        introNote = sans(11.5, lineHeight = 11.5 * 1.68),
        introCta = sans(15.0, FontWeight.SemiBold),
        introSkip = sans(12.5),
        calMonth = serif(KoFestFonts.CormorantGaramond, 25.0, FontWeight.SemiBold),
        calMonthSub = sans(12.5, FontWeight.Medium),
        calWeekday = serif(KoFestFonts.CormorantGaramond, 11.5, italic = true),
        calDay = serif(KoFestFonts.CormorantGaramond, 16.0, FontWeight.SemiBold, lineHeight = 16.0),
        calSelected = serif(titleFamily, 16.0, FontWeight.Bold),
        calSelectedSub = sans(12.0),
        calEmpty = sans(13.5, lineHeight = 13.5 * 1.7),
        myTitle = serif(titleFamily, 24.0, FontWeight.Bold),
        myCountNumber = serif(KoFestFonts.CormorantGaramond, 26.0, FontWeight.SemiBold, lineHeight = 26.0),
        myCountLabel = sans(11.5),
        mySection = serif(titleFamily, 16.0, FontWeight.Bold),
        mySectionSub = sans(12.0),
        myRow = sans(13.5),
        myRowValue = sans(12.5),
        myNote = sans(11.5, lineHeight = 11.5 * 1.7),
        pickBoxTitle = serif(titleFamily, 17.0, FontWeight.Bold),
        pickBoxBody = sans(12.0, lineHeight = 12.0 * 1.55),
        pickBoxCta = sans(13.0, FontWeight.SemiBold),
        pickTitle = serif(titleFamily, 24.0, FontWeight.Bold),
        pickLead = sans(14.0, lineHeight = 14.0 * 1.85),
        pickButton = sans(15.0, FontWeight.SemiBold),
        pickNote = sans(12.0),
        pickCount = serif(KoFestFonts.CormorantGaramond, 13.0),
        pickCardTitle = serif(titleFamily, 21.0, FontWeight.Bold, lineHeight = 21.0 * 1.36),
        pickCardPlace = sans(12.5),
        pickCardKm = serif(KoFestFonts.CormorantGaramond, 13.0),
        pickBadge = sans(11.0, FontWeight.SemiBold),
    )
}

/** Material3 컴포넌트가 참조하는 기본 타이포. 앱 UI 는 [KoFestTypography] 를 직접 쓴다. */
internal val MaterialTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = KoFestFonts.Pretendard),
        bodyMedium = bodyMedium.copy(fontFamily = KoFestFonts.Pretendard),
        bodySmall = bodySmall.copy(fontFamily = KoFestFonts.Pretendard),
        labelLarge = labelLarge.copy(fontFamily = KoFestFonts.Pretendard),
        labelMedium = labelMedium.copy(fontFamily = KoFestFonts.Pretendard),
        labelSmall = labelSmall.copy(fontFamily = KoFestFonts.Pretendard),
        titleLarge = titleLarge.copy(fontFamily = KoFestFonts.Pretendard),
        titleMedium = titleMedium.copy(fontFamily = KoFestFonts.Pretendard),
        titleSmall = titleSmall.copy(fontFamily = KoFestFonts.Pretendard),
    )
}
