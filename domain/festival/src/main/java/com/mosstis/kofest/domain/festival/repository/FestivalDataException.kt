package com.mosstis.kofest.domain.festival.repository

/**
 * 데이터 조회 실패.
 *
 * 실패와 "데이터 없음"을 구분한다 — 빈 결과는 빈 리스트로 오고, 실패만 이 예외로 온다.
 * 둘을 같이 처리하면 재시도 기회를 잃는다.
 *
 * 원인을 삼키지 않기 위해 [message] 에 서버가 준 문구를 그대로 담는다.
 * 서버 로그를 못 보는 상황이 훨씬 많다.
 */
sealed class FestivalDataException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** API 키가 없거나 틀렸다. 재시도해도 소용없다 */
    class Unauthorized(message: String) : FestivalDataException(message)

    /** 연결 실패·타임아웃. 재시도할 가치가 있다 */
    class Network(message: String, cause: Throwable?) : FestivalDataException(message, cause)

    /** 서버가 4xx/5xx 를 냈다 */
    class Server(val code: Int, message: String) : FestivalDataException(message)

    /**
     * 200 인데 앱이 읽을 수 없는 응답이 왔다.
     *
     * 지금은 정상적으로 일어난다 — `/places` `/plan` 이 앱용 JSON 대신 **웹 HTML** 을 준다.
     * 이 경우를 [Server] 와 섞으면 "서버 오류"로 보이므로 나눈다.
     * 화면은 이것을 '아직 준비되지 않은 기능'으로 읽는다.
     */
    class NotReady(message: String, cause: Throwable?) : FestivalDataException(message, cause)
}
