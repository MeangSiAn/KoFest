package com.mosstis.kofest.domain.festival.model

/**
 * 서버가 `GET /home` 에 실어 보내는 앱 설정. 홈 데이터와 성격이 달라 따로 둔다.
 *
 * 홈을 부를 때마다 함께 와서 **배포 없이 서버에서 바꿀 수 있다**는 것이 요점이다.
 */
data class AppConfig(
    /**
     * 이 버전 미만이면 업데이트를 알린다. null 이면 강제하지 않는다.
     * 실제 응답(2026-09-10)은 아직 null 이다.
     */
    val minAppVersion: String?,
    val ads: Ads,
) {
    /**
     * 광고 설정. **[enabled] 가 false 면 광고를 그리지 않는다** — 서버에서 끄고 켠다.
     *
     * 아직 광고 SDK 를 붙이지 않아 읽어두기만 한다. 붙일 때 이 값만 보면 되도록
     * 서버가 주는 형태 그대로 들고 있는다.
     */
    data class Ads(
        val enabled: Boolean,
        /** 목록에서 첫 광고가 들어갈 자리 (행 번호) */
        val listFirstIndex: Int,
        /** 그 뒤로 몇 행마다 넣을지 */
        val listInterval: Int,
        /** 상세 화면에도 넣을지 */
        val onDetail: Boolean,
    )
}
