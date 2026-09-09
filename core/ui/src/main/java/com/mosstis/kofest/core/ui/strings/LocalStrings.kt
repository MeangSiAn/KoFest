package com.mosstis.kofest.core.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.mosstis.kofest.core.common.AppLanguage

val LocalStrings: ProvidableCompositionLocal<KoFestStrings> =
    staticCompositionLocalOf { KoStrings }

fun stringsFor(language: AppLanguage): KoFestStrings = when (language) {
    AppLanguage.KO -> KoStrings
    AppLanguage.EN -> EnStrings
}

/** `strings().nav.home` 처럼 쓴다. */
@Composable
fun strings(): KoFestStrings = LocalStrings.current
