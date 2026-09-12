package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.PickResult
import com.mosstis.kofest.domain.festival.model.RegionBucket
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.PickRepository
import java.util.Locale
import javax.inject.Inject

/**
 * 주변에서 한 곳 뽑기 (기획서 11).
 *
 * 좌표는 **소수점 5자리까지만** 보낸다 — 약 1m 정밀도라 그보다 정확할 이유가 없고,
 * 개인정보 처리방침에도 그렇게 적는다. 이미 나온 곳은 [exclude] 로 빼서 같은 곳이 두 번 나오지 않게 한다.
 */
class PickNearbyPlaceUseCase @Inject constructor(
    private val repository: PickRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        latitude: Double,
        longitude: Double,
        exclude: List<Long>,
    ): PickResult = repository.pick(
        language = language,
        latitude = latitude.round5(),
        longitude = longitude.round5(),
        exclude = exclude.takeLast(MAX_EXCLUDE),
    )

    private fun Double.round5(): Double = String.format(Locale.ROOT, "%.5f", this).toDouble()

    private companion object {
        /** 서버가 받는 상한 */
        const val MAX_EXCLUDE = 200
    }
}

/**
 * 시도 목록. 별도 엔드포인트가 없어 `/home` 의 `regions` 를 쓴다 — 이름과 건수를 서버가 완성해서 준다.
 * 목록 화면의 지역 선택 시트가 부른다.
 */
class GetRegionsUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(language: AppLanguage): List<RegionBucket> =
        repository.getHomeFeed(language).regions
}
