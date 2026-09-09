package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLanguageUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.language
}

class SetLanguageUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
