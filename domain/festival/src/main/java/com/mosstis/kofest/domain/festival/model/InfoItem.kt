package com.mosstis.kofest.domain.festival.model

/**
 * 상세의 '안내' 표 한 줄.
 *
 * 서버가 **빈 값은 키 자체를 내려주지 않는다.** 앱은 온 키만 그리면 되고,
 * 없는 항목의 행을 만들지 않는다.
 */
data class InfoItem(
    val key: InfoKey,
    val value: String,
)

/**
 * 안내 항목 종류. 순서가 곧 화면에 그려지는 순서다 (기획서의 표 순서).
 * `program` 은 별도 섹션이라 여기에 없다.
 */
enum class InfoKey(val apiKey: String) {
    PLACE("place"),
    PLAY_TIME("playTime"),
    FEE("fee"),
    DISCOUNT("discount"),
    SPEND_TIME("spendTime"),
    AGE_LIMIT("ageLimit"),
    BOOKING("booking"),
    PLACE_INFO("placeInfo"),
    SUB_EVENT("subEvent"),
    GRADE("grade"),
    ;

    companion object {
        fun from(apiKey: String): InfoKey? = entries.firstOrNull { it.apiKey == apiKey }
    }
}
