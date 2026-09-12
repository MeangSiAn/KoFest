package com.mosstis.kofest.data.festival.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KoFestApi {

    @GET("festivals")
    suspend fun getFestivals(
        @Query("lang") lang: String,
        @Query("region") region: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("state") state: String? = null,
        @Query("hasImage") hasImage: Boolean? = null,
        @Query("q") query: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = DEFAULT_LIMIT,
    ): FestivalListResponse

    @GET("home")
    suspend fun getHome(
        @Query("lang") lang: String,
    ): HomeResponse

    @GET("festivals/{lang}/{contentId}")
    suspend fun getFestival(
        @Path("lang") lang: String,
        @Path("contentId") contentId: Long,
    ): FestivalDetailResponse

    @GET("health")
    suspend fun getHealth(): HealthResponse

    // ─── 앱용 API 는 `/api/` 아래다. `/places` `/plan` 은 웹 페이지 주소라 같은 경로에 둘 수 없었다
    //     (개발문서3 · 적용방법.md). 2026-09-10 실제 응답으로 확인했다.

    /** 관광지 목록. 커서는 `제목|contentId` */
    @GET("api/places")
    suspend fun getPlaces(
        @Query("lang") lang: String,
        @Query("region") region: String? = null,
        /** **한국어 번호로 보낸다** — 영문 번호(76)를 보내지 않는다 */
        @Query("type") type: Int? = null,
        @Query("hasImage") hasImage: Int? = null,
        @Query("q") query: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = PLACE_LIMIT,
    ): PlaceListResponse

    @GET("api/places/{lang}/{contentId}")
    suspend fun getPlace(
        @Path("lang") lang: String,
        @Path("contentId") contentId: Long,
    ): PlaceDetailResponse

    /** 매거진 목록 */
    @GET("api/stories")
    suspend fun getStories(
        @Query("lang") lang: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = STORY_LIMIT,
    ): StoryListResponse

    /** 매거진 본문. HTML 이 아니라 블록 배열로 온다 */
    @GET("api/stories/{lang}/{slug}")
    suspend fun getStory(
        @Path("lang") lang: String,
        @Path("slug") slug: String,
    ): StoryContentResponse

    /** 자동 일정. 같은 입력이면 같은 결과라 저장하지 않고 다시 부른다 */
    @GET("api/plan")
    suspend fun getPlan(
        @Query("lang") lang: String,
        @Query("area") area: String,
        @Query("from") from: String,
        @Query("nights") nights: Int,
        @Query("mood") mood: String,
    ): PlanResponse

    /**
     * 통계. 한 건씩 보내지 않고 모아서 올린다.
     *
     * 200 이어도 서버가 일부만 받을 수 있으므로 `accepted` 를 돌려받아 보낸 수와 맞춰본다.
     */
    @POST("events")
    suspend fun postEvents(@Body body: EventBatchRequest): EventAcceptedResponse

    companion object {
        /** API 계약상 1–50, 기본 20 */
        const val DEFAULT_LIMIT = 20

        /** 개발문서2 예시값 */
        const val PLACE_LIMIT = 24
        const val STORY_LIMIT = 12
    }
}
