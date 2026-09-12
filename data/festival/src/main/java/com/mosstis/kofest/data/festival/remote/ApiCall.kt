package com.mosstis.kofest.data.festival.remote

import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * 실패를 조용히 삼키지 않는다.
 *
 * 응답 형태만 파싱하고 예외를 `{}` 로 뭉개면 인증 실패가 "빈 성공"으로 읽혀
 * 화면에는 그냥 빈 목록이 뜬다. 원인이 남도록 타입을 나눠 다시 던진다.
 */
internal suspend fun <T> apiCall(
    json: Json,
    block: suspend () -> T,
): T = try {
    block()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (http: HttpException) {
    val body = http.response()?.errorBody()?.string().orEmpty()
    val parsed = runCatching { json.decodeFromString<ApiErrorResponse>(body) }.getOrNull()
    val message = listOfNotNull(parsed?.message, parsed?.hint)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" ")
        ?: body.ifBlank { http.message() }

    when {
        http.code() == HTTP_UNAUTHORIZED -> throw FestivalDataException.Unauthorized(message)
        // 아직 만들어지지 않은 엔드포인트. 서버 오류가 아니라 '준비 중'이다.
        http.code() == HTTP_NOT_FOUND -> throw FestivalDataException.NotReady(message, http)
        else -> throw FestivalDataException.Server(http.code(), message)
    }
} catch (io: IOException) {
    throw FestivalDataException.Network(io.message ?: "네트워크 연결 실패", io)
} catch (serialization: SerializationException) {
    // 200 인데 JSON 이 아니다. `/places` `/plan` 이 앱용 JSON 대신 웹 HTML 을 주는 지금 상태가 그렇다.
    // 여기서 안 잡으면 화면이 그대로 죽는다.
    throw FestivalDataException.NotReady(
        serialization.message ?: "응답을 읽을 수 없습니다",
        serialization,
    )
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_NOT_FOUND = 404
