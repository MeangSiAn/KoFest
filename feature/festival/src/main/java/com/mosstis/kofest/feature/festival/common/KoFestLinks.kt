package com.mosstis.kofest.feature.festival.common

/**
 * 약관·정책은 앱에 박지 않고 웹 페이지를 띄운다. 약관은 바뀌고, 앱에 박으면 한 줄 고치는 데 심사를 다시 받는다.
 *
 * TODO(server): 2026-09-08 기준 아래 경로는 서버에 아직 없다 (`/privacy` 등 → 404, 키 없이는 401).
 *  API 키 없이 열리는 정적 페이지로 올려야 브라우저에서 보인다.
 */
object KoFestLinks {
    private const val BASE = "https://kofest.mosstis.com"
    const val PRIVACY = "$BASE/privacy"
    const val TERMS = "$BASE/terms"
    const val COPYRIGHT = "$BASE/credits"
    const val LICENSES = "$BASE/licenses"
}
