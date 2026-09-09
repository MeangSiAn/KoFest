package com.mosstis.kofest.data.festival.remote

import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import kotlinx.coroutines.CancellationException
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

    if (http.code() == HTTP_UNAUTHORIZED) {
        throw FestivalDataException.Unauthorized(message)
    } else {
        throw FestivalDataException.Server(http.code(), message)
    }
} catch (io: IOException) {
    throw FestivalDataException.Network(io.message ?: "네트워크 연결 실패", io)
}

private const val HTTP_UNAUTHORIZED = 401
