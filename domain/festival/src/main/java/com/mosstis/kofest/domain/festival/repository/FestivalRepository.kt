package com.mosstis.kofest.domain.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.model.FestivalPage
import com.mosstis.kofest.domain.festival.model.FestivalDetail
import com.mosstis.kofest.domain.festival.model.HomeFeed
import com.mosstis.kofest.domain.festival.model.LanguageCounts
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * 축제 데이터 접근. 구현체는 `:data:festival` 에 있고 조립은 `:app` 의 Hilt 그래프가 한다.
 *
 * 실패와 "데이터 없음"을 구분한다 — 실패는 예외로 던지고, 빈 결과는 빈 리스트로 준다.
 * 둘을 같이 처리하면 재시도 기회를 잃는다.
 */
interface FestivalRepository {

    /** 사용자가 고른 언어. 앱을 다시 열어도 유지된다 */
    val language: Flow<AppLanguage>

    /**
     * 언어를 한 번이라도 직접 골랐는가. 최초 실행 판별에 쓴다.
     *
     * 기기 언어로 자동 결정하지 않는 이유는, 이 앱에서 언어가 표시 설정이 아니라
     * **보이는 축제 수(903 vs 235)** 를 정하는 선택이기 때문이다.
     * 영어 기기를 쓰는 한국인이 4분의 1만 보게 된다.
     */
    val hasSelectedLanguage: Flow<Boolean>

    /** 언어를 저장하고 '직접 골랐음'으로 표시한다. 서버에 보내지 않는다 — 계정이 없다 */
    suspend fun setLanguage(language: AppLanguage)

    /**
     * 언어별 축제 수. 인트로의 언어 선택 화면이 숫자를 그대로 보여준다.
     *
     * 매일 04:00 배치로 갱신되므로 화면에 박아두지 않는다.
     * 실패하면 예외를 던진다 — 호출한 쪽이 '숫자 없이' 보여줄지 정한다.
     */
    suspend fun getLanguageCounts(): LanguageCounts

    /** 마지막으로 데이터를 성공적으로 받은 시각. 네트워크 실패 시 안내 띠에 쓴다 */
    val lastSyncedAt: Flow<Instant?>

    suspend fun getHomeFeed(language: AppLanguage): HomeFeed

    suspend fun getFestivals(
        language: AppLanguage,
        filter: FestivalFilter,
        cursor: String?,
        limit: Int,
    ): FestivalPage

    /** 소개·안내·사진·주변 축제가 한 번에 온다. 추가 호출이 없다 */
    suspend fun getFestivalDetail(language: AppLanguage, contentId: Long): FestivalDetail
}
