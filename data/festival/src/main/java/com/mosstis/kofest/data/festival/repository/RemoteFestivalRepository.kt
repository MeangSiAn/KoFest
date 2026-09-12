package com.mosstis.kofest.data.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.data.festival.BuildConfig
import com.mosstis.kofest.data.festival.mapper.toDomain
import com.mosstis.kofest.data.festival.mapper.toDomainOrNull
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import com.mosstis.kofest.data.festival.mapper.toLocalDateOrNull
import com.mosstis.kofest.data.festival.local.LanguagePreferenceStore
import com.mosstis.kofest.data.festival.remote.KoFestApi
import com.mosstis.kofest.data.festival.remote.apiCall
import com.mosstis.kofest.domain.festival.model.AppConfig
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.FestivalPage
import com.mosstis.kofest.domain.festival.model.HomeFeed
import com.mosstis.kofest.domain.festival.model.LanguageCounts
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFestivalRepository @Inject constructor(
    private val api: KoFestApi,
    private val json: Json,
    private val languageStore: LanguagePreferenceStore,
) : FestivalRepository {

    private val _lastSyncedAt = MutableStateFlow<Instant?>(null)
    private val _appConfig = MutableStateFlow<AppConfig?>(null)

    override val language: Flow<AppLanguage> = languageStore.language

    override val hasSelectedLanguage: Flow<Boolean> = languageStore.hasSelected

    override val lastSyncedAt: Flow<Instant?> = _lastSyncedAt.asStateFlow()

    override val appConfig: Flow<AppConfig?> = _appConfig.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        languageStore.set(language)
    }

    override suspend fun getLanguageCounts(): LanguageCounts = apiCall(json) {
        val response = api.getHealth()
        response.counts.toDomain()
    }

    override suspend fun getHomeFeed(language: AppLanguage): HomeFeed = apiCall(json) {
        val response = api.getHome(lang = language.code)

        // 앱 설정은 홈 응답에 얹혀 온다. 홈 데이터와 성격이 다르니 따로 흘려보낸다.
        _appConfig.value = AppConfig(
            minAppVersion = response.minAppVersion,
            ads = AppConfig.Ads(
                enabled = response.ads?.enabled == true,
                listFirstIndex = response.ads?.listFirstIndex ?: 0,
                listInterval = response.ads?.listInterval ?: 0,
                onDetail = response.ads?.onDetail == true,
            ),
        )

        val counts = response.counts.toDomain()

        HomeFeed(
            banners = response.banners.mapNotNull { it.toDomainOrNull(BuildConfig.KOFEST_BASE_URL) },
            ongoing = response.ongoing.toDomain(),
            themes = response.themes.map { it.toDomain(BuildConfig.KOFEST_BASE_URL) },
            picks = response.picks.map { it.toDomain() },
            storyCount = response.storyCount,
            themeCount = response.themeCount,
            regions = response.regions.map { it.toDomain() },
            totalCount = counts.of(language).festival,
            counts = counts,
        ).also { _lastSyncedAt.value = Instant.now() }
    }

    /**
     * 광역권은 시도 여럿이다. **서버가 지역을 하나만 받으므로** 코드별로 부른 뒤 합친다.
     *
     * 커서가 `"2026-10-15|293084"` = `(start_date, content_id)` 이고 정렬이 모든 지역에서 같아서,
     * 합친 결과의 **마지막 항목 하나로 모든 시도의 다음 커서를 만들 수 있다.**
     * 시도별 커서를 따로 들고 다닐 필요가 없다.
     */
    private suspend fun getMergedFestivals(
        language: AppLanguage,
        filter: FestivalFilter,
        cursor: String?,
        limit: Int,
    ): FestivalPage {
        val pages = filter.regionCodes.map { code ->
            getSingleRegionFestivals(language, filter, code, cursor, limit)
        }
        val merged = pages
            .flatMap { it.items }
            .distinctBy { it.contentId }
            .sortedWith(compareBy({ it.startDate }, { it.contentId }))
            .take(limit)

        val exhausted = pages.all { it.nextCursor == null }
        return FestivalPage(
            items = merged,
            // 합친 것이 limit 을 못 채웠고 어느 지역에도 더 없으면 끝이다.
            nextCursor = if (exhausted && merged.size < limit) {
                null
            } else {
                merged.lastOrNull()?.let { "${it.startDate}|${it.contentId}" }
            },
            counts = pages.firstOrNull()?.counts ?: LanguageCounts.EMPTY,
        )
    }

    override suspend fun getFestivals(
        language: AppLanguage,
        filter: FestivalFilter,
        cursor: String?,
        limit: Int,
    ): FestivalPage =
        if (filter.regionCodes.size > 1) {
            getMergedFestivals(language, filter, cursor, limit)
        } else {
            getSingleRegionFestivals(language, filter, filter.primaryRegionCode, cursor, limit)
        }

    private suspend fun getSingleRegionFestivals(
        language: AppLanguage,
        filter: FestivalFilter,
        regionCode: String?,
        cursor: String?,
        limit: Int,
    ): FestivalPage = apiCall(json) {
        val response = api.getFestivals(
            lang = language.code,
            region = regionCode,
            from = filter.from?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            to = filter.to?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            // 서버는 기본으로 종료된 축제를 빼 준다. ongoing 만 볼 때만 state 를 보낸다.
            state = if (filter.ongoingOnly) STATE_ONGOING else null,
            hasImage = if (filter.hasImageOnly) true else null,
            query = filter.query?.takeIf { it.isNotBlank() },
            cursor = cursor,
            limit = limit,
        )

        FestivalPage(
            items = response.items.toDomain(),
            nextCursor = response.nextCursor,
            counts = response.counts.toDomain(),
        ).also { _lastSyncedAt.value = Instant.now() }
    }

    override suspend fun getFestivalDetail(
        language: AppLanguage,
        contentId: Long,
    ): FestivalDetail = apiCall(json) {
        val response = api.getFestival(lang = language.code, contentId = contentId)
        response.toDomain()
            ?: throw FestivalDataException.Server(
                code = HTTP_OK,
                message = "축제 $contentId 의 날짜를 읽지 못했습니다",
            )
    }.also { _lastSyncedAt.value = Instant.now() }


    private companion object {
        const val STATE_ONGOING = "ongoing"

        /** 응답은 200 인데 본문을 못 읽는 경우 */
        const val HTTP_OK = 200
    }
}
