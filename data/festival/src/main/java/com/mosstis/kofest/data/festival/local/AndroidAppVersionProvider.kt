package com.mosstis.kofest.data.festival.local

import android.content.Context
import com.mosstis.kofest.domain.festival.repository.AppVersionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 설치된 APK 의 `versionName`. 읽지 못하면 [UNKNOWN] — 통계와 업데이트 안내가 이 값을 쓴다 */
@Singleton
class AndroidAppVersionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppVersionProvider {

    override val versionName: String by lazy {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: UNKNOWN
    }

    private companion object {
        const val UNKNOWN = "unknown"
    }
}
