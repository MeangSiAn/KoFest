package com.mosstis.kofest.domain.festival.usecase

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.FestivalFilter
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * 달력용 — 그 달에 하루라도 걸치는 축제 전부.
 *
 * 월 단위로 한 번만 부른다. 날짜를 누를 때마다 부르면 31번을 부르게 된다.
 * 몇 해 전에 시작해 지금도 진행중인 상설 행사가 매달 걸치므로 한 페이지(50)를 넘긴다 —
 * 커서가 끝날 때까지 이어 받되 [MAX_PAGES] 로 상한을 둔다.
 *
 * `from`/`to` 를 주면 서버가 종료된 축제도 내려준다 (실제 응답으로 확인). 지난 달도 볼 수 있다.
 *
 * [filter] 는 목록 화면이 걸어둔 것을 그대로 받는다 — 달력은 목록 안의 전환이라 필터를 공유한다.
 * 기간만은 달력이 정한다. 보고 있는 달이 곧 기간이다.
 */
class GetMonthFestivalsUseCase @Inject constructor(
    private val repository: FestivalRepository,
) {
    suspend operator fun invoke(
        language: AppLanguage,
        month: YearMonth,
        filter: FestivalFilter = FestivalFilter(),
    ): List<Festival> {
        val monthFilter = filter.copy(from = month.atDay(1), to = month.atEndOfMonth())
        val items = mutableListOf<Festival>()
        var cursor: String? = null
        repeat(MAX_PAGES) {
            val page = repository.getFestivals(language, monthFilter, cursor, PAGE_SIZE)
            items += page.items
            cursor = page.nextCursor ?: return items.distinctBy { it.contentId }
        }
        return items.distinctBy { it.contentId }
    }

    private companion object {
        /** API 계약의 최대값 */
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 8
    }
}
