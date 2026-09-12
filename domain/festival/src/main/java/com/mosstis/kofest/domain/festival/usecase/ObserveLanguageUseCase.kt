package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.analytics.AppEvent
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLanguageUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.language
}

/**
 * 언어 전환은 헤더 세그먼트·MY 설정·빈 화면 버튼 여러 곳에서 일어난다.
 * 기록을 화면마다 붙이면 빠뜨리는 곳이 생기므로 여기 한 곳에서 남긴다.
 */
class SetLanguageUseCase @Inject constructor(
    private val repository: FestivalRepository,
    private val tracker: EventTracker,
) {
    suspend operator fun invoke(language: AppLanguage) {
        repository.setLanguage(language)
        tracker.track(AppEvent.LANG_SWITCH)
    }
}
