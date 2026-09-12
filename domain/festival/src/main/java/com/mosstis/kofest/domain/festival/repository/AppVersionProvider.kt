package com.mosstis.kofest.domain.festival.repository

/** 설치된 앱의 `versionName`. 구현은 `:data:festival` 에서 PackageManager 로 읽는다 */
interface AppVersionProvider {
    val versionName: String
}
