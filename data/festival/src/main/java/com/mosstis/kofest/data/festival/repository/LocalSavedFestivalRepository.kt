package com.mosstis.kofest.data.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.data.festival.local.SavedFestivalEntity
import com.mosstis.kofest.data.festival.local.SavedFestivalStore
import com.mosstis.kofest.data.festival.mapper.toFestivalStateOrUpcoming
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.SavedFestival
import com.mosstis.kofest.domain.festival.repository.SavedFestivalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSavedFestivalRepository @Inject constructor(
    private val store: SavedFestivalStore,
) : SavedFestivalRepository {

    override fun observeSaved(): Flow<List<SavedFestival>> =
        store.saved.map { entities -> entities.mapNotNull { it.toDomainOrNull() } }

    override fun isSaved(language: AppLanguage, contentId: Long): Flow<Boolean> =
        store.saved.map { entities ->
            entities.any { it.lang == language.code && it.contentId == contentId }
        }

    override suspend fun toggle(language: AppLanguage, festival: Festival): Boolean =
        store.toggle(festival.toEntity(language))

    private fun Festival.toEntity(language: AppLanguage) = SavedFestivalEntity(
        lang = language.code,
        contentId = contentId,
        title = title,
        region = region,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        thumbUrl = thumbUrl,
        imageUrl = imageUrl,
        state = state.name.lowercase(),
        savedAt = System.currentTimeMillis(),
    )

    private fun SavedFestivalEntity.toDomainOrNull(): SavedFestival? {
        val start = runCatching { LocalDate.parse(startDate) }.getOrNull() ?: return null
        val end = runCatching { LocalDate.parse(endDate) }.getOrNull() ?: start

        return SavedFestival(
            language = AppLanguage.from(lang),
            festival = Festival(
                contentId = contentId,
                title = title,
                region = region,
                startDate = start,
                endDate = end,
                thumbUrl = thumbUrl,
                imageUrl = imageUrl,
                state = state.toFestivalStateOrUpcoming(),
            ),
            savedAtEpochMillis = savedAt,
        )
    }
}
