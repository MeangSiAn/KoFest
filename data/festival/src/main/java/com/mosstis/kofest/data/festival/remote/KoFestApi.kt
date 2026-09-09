package com.mosstis.kofest.data.festival.remote

import retrofit2.http.GET
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

    companion object {
        /** API 계약상 1–50, 기본 20 */
        const val DEFAULT_LIMIT = 20
    }
}
