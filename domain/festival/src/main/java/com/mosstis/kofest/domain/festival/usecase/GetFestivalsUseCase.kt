package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.FestivalPage
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import javax.inject.Inject

class GetFestivalsUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        filter: FestivalFilter,
        cursor: String? = null,
        limit: Int = DEFAULT_LIMIT,
    ): FestivalPage = repository.getFestivals(language, filter, cursor, limit)

    private companion object {
        /** API 계약상 1–50, 기본 20 */
        const val DEFAULT_LIMIT = 20
    }
}
