package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.PlaceDetail
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import com.mosstis.kofest.domain.festival.model.PlacePage
import com.mosstis.kofest.domain.festival.model.StoryContent
import com.mosstis.kofest.domain.festival.model.StoryPage
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.model.TravelPlanRequest
import com.mosstis.kofest.domain.festival.repository.PlaceRepository
import com.mosstis.kofest.domain.festival.repository.PlanRepository
import com.mosstis.kofest.domain.festival.repository.StoryRepository
import javax.inject.Inject

class GetStoriesUseCase @Inject constructor(
    private val repository: StoryRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        cursor: String? = null,
        limit: Int = DEFAULT_LIMIT,
    ): StoryPage = repository.getStories(language, cursor, limit)

    private companion object {
        const val DEFAULT_LIMIT = 12
    }
}

class GetStoryUseCase @Inject constructor(
    private val repository: StoryRepository,
) {
    suspend operator fun invoke(language: AppLanguage, slug: String): StoryContent =
        repository.getStory(language, slug)
}

class GetPlacesUseCase @Inject constructor(
    private val repository: PlaceRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        filter: PlaceFilter,
        cursor: String? = null,
        limit: Int = DEFAULT_LIMIT,
    ): PlacePage = repository.getPlaces(language, filter, cursor, limit)

    private companion object {
        const val DEFAULT_LIMIT = 24
    }
}

class GetPlaceDetailUseCase @Inject constructor(
    private val repository: PlaceRepository,
) {
    suspend operator fun invoke(language: AppLanguage, contentId: Long): PlaceDetail =
        repository.getPlaceDetail(language, contentId)
}

/**
 * 자동 일정. 같은 입력이면 같은 결과라 **결과를 저장하지 않고** 조건만 들고 있다가 다시 부른다.
 */
class GetTravelPlanUseCase @Inject constructor(
    private val repository: PlanRepository,
) {
    suspend operator fun invoke(language: AppLanguage, request: TravelPlanRequest): TravelPlan =
        repository.getPlan(language, request)
}
