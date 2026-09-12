package com.mosstis.kofest.domain.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.PlaceDetail
import com.mosstis.kofest.domain.festival.model.PlaceFilter
import com.mosstis.kofest.domain.festival.model.PlacePage
import com.mosstis.kofest.domain.festival.model.Story
import com.mosstis.kofest.domain.festival.model.StoryContent
import com.mosstis.kofest.domain.festival.model.StoryPage
import com.mosstis.kofest.domain.festival.model.TravelPlan
import com.mosstis.kofest.domain.festival.model.TravelPlanRequest

/**
 * 축제 말고 나머지 — 관광지 · 테마 · 매거진 · 자동 일정.
 *
 * 축제(`FestivalRepository`)와 나눈 이유는 **서버가 아직 이 넷을 내려주지 않기 때문**이다.
 * 여기서 나는 실패는 화면을 "준비 중"으로 만들 뿐 축제 화면을 건드리지 않아야 한다.
 */
interface PlaceRepository {
    suspend fun getPlaces(
        language: AppLanguage,
        filter: PlaceFilter,
        cursor: String?,
        limit: Int,
    ): PlacePage

    suspend fun getPlaceDetail(language: AppLanguage, contentId: Long): PlaceDetail
}

interface StoryRepository {
    suspend fun getStories(language: AppLanguage, cursor: String?, limit: Int): StoryPage

    suspend fun getStory(language: AppLanguage, slug: String): StoryContent

    /** 홈 아래쪽에 붙는 최신 3건 */
    suspend fun getLatest(language: AppLanguage, count: Int): List<Story>
}

interface PlanRepository {
    suspend fun getPlan(language: AppLanguage, request: TravelPlanRequest): TravelPlan
}
