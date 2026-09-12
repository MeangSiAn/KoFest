package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppVersion
import com.mosstis.kofest.domain.festival.repository.AppVersionProvider
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 새 버전이 나왔는가. MY 의 앱 정보 줄에 "최신 / 업데이트 있음"으로 나온다.
 *
 * 홈을 한 번이라도 부른 뒤에야 값이 생긴다 (설정이 `GET /home` 에 실려 온다).
 * 그전에는 false — 모르는 상태에서 "업데이트 있음"을 띄우지 않는다.
 */
class ObserveUpdateAvailableUseCase @Inject constructor(
    private val repository: FestivalRepository,
    private val appVersion: AppVersionProvider,
) {
    operator fun invoke(): Flow<Boolean> = repository.appConfig.map { config ->
        AppVersion.isOlder(current = appVersion.versionName, required = config?.minAppVersion)
    }
}
