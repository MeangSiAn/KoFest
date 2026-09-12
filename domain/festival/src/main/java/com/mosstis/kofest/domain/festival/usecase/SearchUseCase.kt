package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import com.mosstis.kofest.domain.festival.model.SearchResult
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.PlaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * 축제와 관광지를 **동시에** 부른다. 순서대로 부르면 대기 시간이 두 배가 된다.
 *
 * 한쪽이 실패해도 다른 쪽은 살린다 — 관광지가 아직 없는 지역이 있어
 * 그쪽 실패로 축제 결과까지 날리면 안 된다.
 */
class SearchUseCase @Inject constructor(
    private val festivalRepository: FestivalRepository,
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        query: String,
        limit: Int = DEFAULT_LIMIT,
    ): SearchResult = coroutineScope {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@coroutineScope SearchResult(trimmed, emptyList(), emptyList())

        val festivals = async {
            runCatching {
                festivalRepository.getFestivals(
                    language = language,
                    filter = FestivalFilter(query = trimmed),
                    cursor = null,
                    limit = limit,
                ).items
            }
        }
        val places = async {
            runCatching {
                placeRepository.getPlaces(
                    language = language,
                    filter = PlaceFilter(query = trimmed),
                    cursor = null,
                    limit = limit,
                ).items
            }
        }

        val festivalResult = festivals.await()
        val placeResult = places.await()

        SearchResult(
            query = trimmed,
            festivals = festivalResult.getOrDefault(emptyList()),
            places = placeResult.getOrDefault(emptyList()),
            festivalFailed = festivalResult.isFailure,
            placeFailed = placeResult.isFailure,
        )
    }

    private companion object {
        /** 기획서 10: 종류별로 8건씩 보여주고 더 있으면 '전체 보기' 로 넘긴다 */
        const val DEFAULT_LIMIT = 8
    }
}
