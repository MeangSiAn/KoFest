package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.domain.festival.model.LanguageCounts
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 최초 실행이면 언어를 묻고, 아니면 바로 홈으로 보낸다. */
class ObserveLanguageSelectedUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.hasSelectedLanguage
}

/** 언어 선택 화면의 903 / 235. 숫자를 하드코딩하면 시간이 지나며 거짓말이 된다. */
class GetLanguageCountsUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(): LanguageCounts = repository.getLanguageCounts()
}
