package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.SavedFestival
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.SavedFestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFestivalDetailUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(language: AppLanguage, contentId: Long): FestivalDetail =
        repository.getFestivalDetail(language, contentId)
}

class ObserveSavedFestivalsUseCase @Inject constructor(
    private val repository: SavedFestivalRepository,
) {
    operator fun invoke(): Flow<List<SavedFestival>> = repository.observeSaved()
}

class IsFestivalSavedUseCase @Inject constructor(
    private val repository: SavedFestivalRepository,
) {
    operator fun invoke(language: AppLanguage, contentId: Long): Flow<Boolean> =
        repository.isSaved(language, contentId)
}

class ToggleSavedFestivalUseCase @Inject constructor(
    private val repository: SavedFestivalRepository,
) {
    suspend operator fun invoke(language: AppLanguage, festival: Festival): Boolean =
        repository.toggle(language, festival)
}
