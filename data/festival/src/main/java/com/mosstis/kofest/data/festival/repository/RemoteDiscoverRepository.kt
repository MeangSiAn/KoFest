package com.mosstis.kofest.data.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.data.festival.BuildConfig
import com.mosstis.kofest.data.festival.mapper.toDomain
import com.mosstis.kofest.data.festival.mapper.toDomainOrNull
import com.mosstis.kofest.data.festival.remote.KoFestApi
import com.mosstis.kofest.data.festival.remote.apiCall
import com.mosstis.kofest.domain.festival.model.PickResult
import com.mosstis.kofest.domain.festival.model.PlaceDetail
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import com.mosstis.kofest.domain.festival.model.PlacePage
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.domain.festival.model.StoryContent
import com.mosstis.kofest.domain.festival.model.StoryPage
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.model.TravelPlanRequest
import com.mosstis.kofest.domain.festival.repository.PickRepository
import com.mosstis.kofest.domain.festival.repository.PlaceRepository
import com.mosstis.kofest.domain.festival.repository.PlanRepository
import com.mosstis.kofest.domain.festival.repository.StoryRepository
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 관광지 · 매거진 · 자동 일정. 전부 `/api/` 아래다 (개발문서3).
 *
 * 404 나 HTML 이 오면 [apiCall] 이 `NotReady` 로 바꾼다 — 서버가 잠깐 옛 버전으로 돌아가도
 * 앱이 죽지 않고 원인이 로그에 남는다.
 */
@Singleton
class RemoteDiscoverRepository @Inject constructor(
    private val api: KoFestApi,
    private val json: Json,
) : PlaceRepository, StoryRepository, PlanRepository, PickRepository {

    override suspend fun getPlaces(
        language: AppLanguage,
        filter: PlaceFilter,
        cursor: String?,
        limit: Int,
    ): PlacePage = apiCall(json) {
        val response = api.getPlaces(
            lang = language.code,
            region = filter.regionCode,
            type = filter.type,
            hasImage = if (filter.hasImageOnly) 1 else null,
            query = filter.query?.takeIf { it.isNotBlank() },
            cursor = cursor,
            limit = limit,
        )
        PlacePage(
            items = response.items.map { it.toDomain() },
            nextCursor = response.nextCursor?.takeIf { it.isNotBlank() },
            total = response.total,
        )
    }

    override suspend fun getPlaceDetail(language: AppLanguage, contentId: Long): PlaceDetail =
        apiCall(json) {
            val response = api.getPlace(lang = language.code, contentId = contentId)
            PlaceDetail(
                place = response.place.toDomain(),
                // 지금은 항상 null 이다. 화면이 "준비 중" 안내로 대신한다.
                overview = response.detail?.overview?.takeIf { it.isNotBlank() },
                nearby = response.nearby.map { it.toDomain() },
            )
        }


    override suspend fun getStories(
        language: AppLanguage,
        cursor: String?,
        limit: Int,
    ): StoryPage = apiCall(json) {
        val response = api.getStories(lang = language.code, cursor = cursor, limit = limit)
        StoryPage(
            items = response.items.map { it.toDomain(BuildConfig.KOFEST_BASE_URL) },
            nextCursor = response.nextCursor?.takeIf { it.isNotBlank() },
            total = response.total,
        )
    }

    override suspend fun getStory(language: AppLanguage, slug: String): StoryContent =
        apiCall(json) {
            val response = api.getStory(lang = language.code, slug = slug)
            StoryContent(
                story = response.story.toDomain(BuildConfig.KOFEST_BASE_URL),
                // 모르는 블록은 버린다. 서버가 종류를 늘려도 옛 앱이 안 깨진다.
                blocks = response.blocks.mapNotNull { it.toDomainOrNull() },
            )
        }

    override suspend fun getLatest(language: AppLanguage, count: Int): List<Story> =
        getStories(language, cursor = null, limit = count).items.take(count)

    override suspend fun getPlan(
        language: AppLanguage,
        request: TravelPlanRequest,
    ): TravelPlan = apiCall(json) {
        api.getPlan(
            lang = language.code,
            area = request.area.code,
            from = request.from.format(DateTimeFormatter.ISO_LOCAL_DATE),
            nights = request.nights,
            mood = request.mood.code,
        ).toDomain()
    }

    override suspend fun pick(
        language: AppLanguage,
        latitude: Double,
        longitude: Double,
        exclude: List<Long>,
    ): PickResult = apiCall(json) {
        api.pick(
            lang = language.code,
            lat = latitude,
            lng = longitude,
            exclude = exclude.takeIf { it.isNotEmpty() }?.joinToString(","),
        ).toDomain()
    }
}
