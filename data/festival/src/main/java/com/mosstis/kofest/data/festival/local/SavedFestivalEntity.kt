package com.mosstis.kofest.data.festival.local

import kotlinx.serialization.Serializable

/**
 * 저장한 축제를 파일에 넣기 위한 형태.
 *
 * contentId 만 저장하지 않고 **목록에 필요한 값을 통째로** 담는다.
 * id 만 두면 저장 탭을 그릴 때 건마다 상세를 호출해야 하고, 오프라인에서는 아무것도 못 그린다.
 * 축제 정보는 하루 단위로 갱신되는 데이터라 저장 시점 값을 보여줘도 대부분 맞다.
 */
@Serializable
internal data class SavedFestivalEntity(
    val lang: String,
    val contentId: Long,
    val title: String,
    val region: String,
    val startDate: String,
    val endDate: String,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val state: String,
    val savedAt: Long,
)
