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

    override val language: Flow<AppLanguage> = languageStore.language

    override val hasSelectedLanguage: Flow<Boolean> = languageStore.hasSelected

    override val lastSyncedAt: Flow<Instant?> = _lastSyncedAt.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        languageStore.set(language)
    }

    override suspend fun getLanguageCounts(): LanguageCounts = apiCall(json) {
        val response = api.getHealth()
        LanguageCounts(ko = response.counts.ko, en = response.counts.en)
    }

    override suspend fun getHomeFeed(language: AppLanguage): HomeFeed = apiCall(json) {
        val response = api.getHome(lang = language.code)

        val weekendFrom = response.weekend.from.toLocalDateOrNull() ?: LocalDate.now()
        val weekendTo = response.weekend.to.toLocalDateOrNull() ?: weekendFrom
        val counts = LanguageCounts(ko = response.counts.ko, en = response.counts.en)

        HomeFeed(
            banners = response.banners.mapNotNull { it.toDomainOrNull(BuildConfig.KOFEST_BASE_URL) },
            ongoing = response.ongoing.toDomain(),
            weekend = response.weekend.items.toDomain(),
            weekendRange = weekendFrom..weekendTo,
            weekendIsNext = weekendFrom.isAfter(comingSaturday(LocalDate.now())),
            regions = response.regions.map { it.toDomain() },
            totalCount = counts.of(language),
            counts = counts,
        ).also { _lastSyncedAt.value = Instant.now() }
    }

    override suspend fun getFestivals(
        language: AppLanguage,
        filter: FestivalFilter,
        cursor: String?,
        limit: Int,
    ): FestivalPage = apiCall(json) {
        val response = api.getFestivals(
            lang = language.code,
            region = filter.regionCode,
            from = filter.from?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            to = filter.to?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            // 서버는 기본으로 종료된 축제를 빼 준다. ongoing 만 볼 때만 state 를 보낸다.
            state = if (filter.ongoingOnly) STATE_ONGOING else null,
            hasImage = if (filter.hasImageOnly) true else null,
            cursor = cursor,
            limit = limit,
        )

        FestivalPage(
            items = response.items.toDomain(),
            nextCursor = response.nextCursor,
            counts = LanguageCounts(ko = response.counts.ko, en = response.counts.en),
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

    /**
     * 서버가 주말 범위를 정해서 준다. 그 시작일이 이번 주 토요일보다 뒤면 '다음 주말'이다.
     * (금~일에는 서버가 다음 주말을 내려준다)
     */
    private fun comingSaturday(today: LocalDate): LocalDate {
        val daysUntilSaturday = (DayOfWeek.SATURDAY.value - today.dayOfWeek.value + 7) % 7
        return today.plusDays(daysUntilSaturday.toLong())
    }

    private fun LanguageCounts.of(language: AppLanguage): Int = when (language) {
        AppLanguage.KO -> ko
        AppLanguage.EN -> en
    }

    private companion object {
        const val STATE_ONGOING = "ongoing"

        /** 응답은 200 인데 본문을 못 읽는 경우 */
        const val HTTP_OK = 200
    }
}
