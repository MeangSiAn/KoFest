package com.mosstis.kofest.domain.festival.model

/**
 * 홈 "지역으로 찾기" 한 칸 — 광역권 하나와 그 안 시도들의 건수 합.
 *
 * 서버는 시도 16개를 그대로 준다. 그대로 늘어놓으면 목록이 길고,
 * 수집 쪽 합성 코드(`12` "전남광주통합특별시")가 화면에 그대로 나온다.
 */
data class RegionGroupBucket(
    val group: RegionGroup,
    val codes: List<String>,
    val count: Int,
)

/**
 * 시도 버킷을 광역권으로 묶는다. **묶지 못한 코드는 버리지 않고** 그대로 한 칸으로 남긴다 —
 * 새 시도코드가 생겼을 때 조용히 사라지면 건수가 안 맞는 이유를 찾기 어렵다.
 */
fun List<RegionBucket>.groupByRegion(): Pair<List<RegionGroupBucket>, List<RegionBucket>> {
    val grouped = mutableMapOf<RegionGroup, MutableList<RegionBucket>>()
    val ungrouped = mutableListOf<RegionBucket>()

    forEach { bucket ->
        val group = RegionGroup.of(bucket.code)
        if (group == null) ungrouped += bucket else grouped.getOrPut(group) { mutableListOf() } += bucket
    }

    val buckets = RegionGroup.entries.mapNotNull { group ->
        val items = grouped[group] ?: return@mapNotNull null
        RegionGroupBucket(
            group = group,
            codes = items.map { it.code },
            count = items.sumOf { it.count },
        )
    }.sortedByDescending { it.count }

    return buckets to ungrouped
}
