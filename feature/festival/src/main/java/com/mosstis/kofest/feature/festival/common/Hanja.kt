package com.mosstis.kofest.feature.festival.common

/**
 * 사진이 없을 때 얹는 한자 한 글자.
 *
 * TourAPI 에 이런 필드는 없다. 제목의 키워드로 고르고 못 고르면 `祭` 를 쓴다.
 * 표시 전용 규칙이라 도메인이 아니라 화면 쪽에 둔다.
 */
object Hanja {

    private val byKeyword: List<Pair<String, String>> = listOf(
        "유등" to "燈", "등불" to "燈",
        "빛" to "光", "조명" to "光",
        "불꽃" to "火", "불빛" to "火", "들불" to "火", "산불" to "火",
        "탈춤" to "舞", "마임" to "舞", "무용" to "舞",
        "국악" to "樂", "음악" to "樂", "재즈" to "樂", "락" to "樂",
        "쌀" to "米", "지평선" to "穗", "억새" to "穗", "보리" to "穗",
        "차" to "茶", "다향" to "茶",
        "김장" to "漬", "젓갈" to "漬", "김치" to "漬",
        "맥주" to "麥", "치맥" to "麥",
        "송어" to "魚", "산천어" to "魚", "빙어" to "魚",
        "옹기" to "陶", "도자" to "陶",
        "화성" to "城", "성곽" to "城",
        "바다" to "海", "한산" to "海", "해변" to "海",
        "꽃" to "花", "벚꽃" to "花", "장미" to "花",
        "눈" to "雪", "얼음" to "氷",
        "가을" to "秋", "단풍" to "秋",
        "춘향" to "香",
    )

    private const val FALLBACK = "祭"

    fun forTitle(title: String): String =
        byKeyword.firstOrNull { (keyword, _) -> title.contains(keyword) }?.second ?: FALLBACK
}
