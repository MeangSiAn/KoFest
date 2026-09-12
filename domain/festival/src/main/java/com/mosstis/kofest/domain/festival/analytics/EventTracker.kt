package com.mosstis.kofest.domain.festival.analytics

/**
 * 화면·행동 기록. 구현체는 `:data:festival` 에 있다.
 *
 * 한 건씩 보내지 않고 **모았다가 20~30개씩** 올린다 (개발문서 3장). 그래서 [track] 은
 * 던져놓고 잊는 호출이다 — 화면이 결과를 기다리지 않는다.
 *
 * 기록에 쓰는 `deviceId` 는 앱이 만든 무작위 문자열이다.
 * 광고 식별자(GAID)를 쓰지 않는다 — 개인정보 처리방침에 그렇게 적혀 있다.
 */
interface EventTracker {

    /** 큐에 쌓는다. 임계치를 넘으면 알아서 전송한다. 실패해도 화면에 영향이 없다 */
    fun track(event: AppEvent, contentId: Long? = null)

    /**
     * 쌓인 것을 지금 보낸다. 앱이 화면 밖으로 나갈 때 부른다.
     *
     * 화면의 코루틴이 아니라 tracker 자신의 스코프에서 돈다 —
     * Activity 가 사라지면서 전송이 취소되면 그 배치를 다음 실행까지 못 보낸다.
     */
    fun flush()
}
