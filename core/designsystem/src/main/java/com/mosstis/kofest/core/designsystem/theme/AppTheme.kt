package com.mosstis.kofest.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.mosstis.kofest.core.common.AppLanguage

/**
 * 기획서의 색을 그대로 쓰기 위해 **Dynamic Color(Material You)를 쓰지 않는다.**
 * 기기 배경색에 따라 자주(#6B1E32)가 덮이면 이 앱의 인상이 사라진다.
 *
 * 다크 테마도 아직 정의하지 않았다. 기획서가 종이색 단일 팔레트를 전제로 하고 있어
 * 라이트 팔레트 하나로 고정한다. 다크가 필요해지면 그때 토큰을 한 벌 더 정의한다.
 */
private val KoFestColorScheme = lightColorScheme(
    primary = KoFestColors.Jaju,
    onPrimary = KoFestColors.OnJaju,
    secondary = KoFestColors.Hwangto,
    onSecondary = KoFestColors.OnJaju,
    background = KoFestColors.Paper,
    onBackground = KoFestColors.Ink,
    surface = KoFestColors.Paper,
    onSurface = KoFestColors.Ink,
    surfaceVariant = KoFestColors.NoticeBackground,
    onSurfaceVariant = KoFestColors.Muted,
    outline = KoFestColors.Line,
    outlineVariant = KoFestColors.Line,
    error = KoFestColors.Jaju,
    onError = KoFestColors.OnJaju,
)

val LocalKoFestTypography: ProvidableCompositionLocal<KoFestTypography> =
    staticCompositionLocalOf { koFestTypographyFor(koreanFirst = true) }

val LocalAppLanguage: ProvidableCompositionLocal<AppLanguage> =
    staticCompositionLocalOf { AppLanguage.KO }

@Composable
fun AppTheme(
    language: AppLanguage = AppLanguage.KO,
    content: @Composable () -> Unit,
) {
    val typography = remember(language) {
        koFestTypographyFor(koreanFirst = language == AppLanguage.KO)
    }

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalKoFestTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = KoFestColorScheme,
            typography = MaterialTypography,
            content = content,
        )
    }
}

/** `KoFestTheme.type.rowTitle` 처럼 짧게 쓰기 위한 접근자. */
object KoFestTheme {
    val type: KoFestTypography
        @Composable get() = LocalKoFestTypography.current

    val language: AppLanguage
        @Composable get() = LocalAppLanguage.current
}
