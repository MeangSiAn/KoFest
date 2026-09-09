package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.HomeFeed
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import javax.inject.Inject

class GetHomeFeedUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(language: AppLanguage): HomeFeed =
        repository.getHomeFeed(language)
}
