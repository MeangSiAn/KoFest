package com.mosstis.kofest.core.common

/**
 * 앱이 보여줄 축제 데이터의 언어.
 *
 * 시스템 로케일이 아니라 사용자가 헤더 세그먼트로 직접 고른다.
 * TourAPI 가 언어별로 별개 서비스이고 contentid 도 다르기 때문에
 * 언어는 "표시 설정"이 아니라 "어느 데이터셋을 보는가"에 가깝다.
 */
enum class AppLanguage(val code: String) {
    KO("ko"),
    EN("en"),
    ;

    companion object {
        fun from(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: KO
    }
}
