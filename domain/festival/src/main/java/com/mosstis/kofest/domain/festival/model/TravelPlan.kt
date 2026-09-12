package com.mosstis.kofest.domain.festival.model

import java.time.LocalDate

/**
 * 자동 일정. `GET /plan` (개발문서2 3장, 기획서 09).
 *
 * **같은 입력이면 같은 결과다.** 서버가 무작위를 쓰지 않으므로 일정을 통째로 저장하지 않는다 —
 * [TravelPlanRequest] 만 들고 있으면 언제든 다시 만들 수 있다.
 */
data class TravelPlan(
    /** 서버가 언어에 맞춰 준 권역 이름 ("서울"). 앱이 코드→이름을 갖지 않는다 */
    val areaName: String?,
    /** "축제 위주로" */
    val moodName: String?,
    val total: Int,
    val festivalCount: Int,
    /** true 면 "이 지역은 아직 관광지 정보가 적습니다" 를 띄운다. 빈 일정을 그럴듯하게 꾸미지 않는다 */
    val thin: Boolean,
    val days: List<Day>,
) {
    data class Day(
        val date: LocalDate,
        /** 그날 주로 머무는 곳. "종로구" */
        val label: String?,
        val stops: List<Stop>,
    )

    /**
     * 한 곳. `meal` 은 **누를 수 없다** — 맛집 데이터가 없어 자리만 비워둔 것이다.
     * 모르는 kind 는 만들지 않고 건너뛴다.
     */
    data class Stop(
        val kind: Kind,
        /** "14:00" */
        val time: String?,
        val contentId: Long?,
        val title: String,
        val region: String?,
        /** 머무는 시간(분) */
        val stayMinutes: Int?,
        val thumbUrl: String?,
        val move: Move?,
    )

    enum class Kind { FESTIVAL, PLACE, MEAL }

    /** 다음 곳까지 가는 법. `transit` 이면 [minutes] 가 **null 이다** */
    data class Move(
        val mode: Mode,
        val minutes: Int?,
        val km: Double?,
    )

    enum class Mode { WALK, TRANSIT }
}

/**
 * 일정 조건. MY 에는 결과가 아니라 **이것만** 저장한다.
 *
 * 광역권은 7개다 — 홈의 지역 묶음(8개)과 다르다. 기획서 09 가 부산·울산을 경상에 넣는다.
 */
data class TravelPlanRequest(
    val area: Area,
    val from: LocalDate,
    /** 0~3박 */
    val nights: Int,
    val mood: Mood,
) {
    /** 시도 17개로 나누면 후보가 모자라 일정이 빈다. "충청" 이면 충북·충남·대전·세종을 함께 본다 */
    enum class Area(val code: String) {
        SEOUL("seoul"),
        GYEONGGI("gyeonggi"),
        GANGWON("gangwon"),
        CHUNGCHEONG("chungcheong"),
        JEOLLA("jeolla"),
        GYEONGSANG("gyeongsang"),
        JEJU("jeju"),
        ;

        companion object {
            fun from(code: String?): Area? = entries.firstOrNull { it.code == code }
        }
    }

    /** 이름만으로는 뭐가 다른지 모르므로 화면에 설명을 함께 붙인다 */
    enum class Mood(val code: String) {
        FESTIVAL("festival"),
        EASY("easy"),
        MANY("many"),
        ;

        companion object {
            fun from(code: String?): Mood? = entries.firstOrNull { it.code == code }
        }
    }
}
